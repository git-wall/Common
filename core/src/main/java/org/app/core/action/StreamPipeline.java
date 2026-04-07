package org.app.core.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * StreamPipeline — dành cho data lớn (file upload, video, CSV triệu dòng,
 * API response streaming, S3 multipart upload...)
 * <p>
 * Nguyên tắc: KHÔNG bao giờ load hết vào memory.
 * Mỗi chunk được xử lý, transform, rồi ghi ra sink — xong thì GC.
 * <p>
 * Use case:
 * StreamPipeline.from(inputStream)
 * .chunkBy(8192)                         // 8KB chunks
 * .map(bytes -> parseChunk(bytes))        // transform mỗi chunk
 * .filter(chunk -> chunk.isValid())       // bỏ chunk lỗi
 * .sink(Sinks.toFile(outputPath))         // ghi ra file
 * .onProgress(progress -> sendSse(progress))
 * .onComplete(summary -> notifyDone(summary))
 * .execute();
 */
public class StreamPipeline<T> {

    private final StreamSource<T> source;
    private final List<Function<T, T>> transformers = new ArrayList<>();
    private final List<Predicate<T>> filters = new ArrayList<>();
    private StreamSink<T> sink;
    private Consumer<StreamProgress> progressCallback;
    private Consumer<StreamSummary> completeCallback;
    private Consumer<StreamError> errorCallback;
    private long chunkSize = 8192;

    private StreamPipeline(StreamSource<T> source) {
        this.source = source;
    }

    // ─── Entry points ───────────────────────────────────────────────────────

    public static StreamPipeline<byte[]> from(InputStream inputStream) {
        return new StreamPipeline<>(new InputStreamSource(inputStream));
    }

    public static StreamPipeline<String> fromLines(InputStream inputStream) {
        return new StreamPipeline<>(new LineSource(inputStream));
    }

    public static StreamPipeline<byte[]> from(Path filePath) throws IOException {
        return from(Files.newInputStream(filePath));
    }

    // ─── Chunking ───────────────────────────────────────────────────────────

    /**
     * Đặt chunk size cho byte streams
     */
    public StreamPipeline<T> chunkBy(long bytes) {
        this.chunkSize = bytes;
        return this;
    }

    // ─── Transform ──────────────────────────────────────────────────────────

    /**
     * map: transform mỗi chunk.
     * Lưu ý: cast không safe ở đây — đây là tradeoff cho API gọn.
     * Với type safety hoàn toàn cần dùng StreamPipeline<U> riêng.
     */
    @SuppressWarnings("unchecked")
    public <U> StreamPipeline<U> map(Function<T, U> mapper) {
        StreamPipeline<U> next = (StreamPipeline<U>) this;
        next.transformers.add(chunk -> mapper.apply(chunk));
        return next;
    }

    /**
     * filter: bỏ qua chunk không pass điều kiện
     */
    public StreamPipeline<T> filter(Predicate<T> predicate) {
        this.filters.add(predicate);
        return this;
    }

    // ─── Sinks ──────────────────────────────────────────────────────────────

    public StreamPipeline<T> sink(StreamSink<T> sink) {
        this.sink = sink;
        return this;
    }

    // ─── Callbacks ──────────────────────────────────────────────────────────

    /**
     * onProgress: gọi sau mỗi chunk với thông tin tiến độ.
     * Dùng để: push SSE cho FE, log progress, update DB status.
     */
    public StreamPipeline<T> onProgress(Consumer<StreamProgress> callback) {
        this.progressCallback = callback;
        return this;
    }

    /**
     * onComplete: gọi khi toàn bộ stream xong.
     * Dùng để: ghi final DB class, send notify, push to S3 complete.
     */
    public StreamPipeline<T> onComplete(Consumer<StreamSummary> callback) {
        this.completeCallback = callback;
        return this;
    }

    /**
     * onError: gọi khi có lỗi giữa chừng.
     * Stream dừng, cleanup được gọi, rồi mới gọi onError.
     */
    public StreamPipeline<T> onError(Consumer<StreamError> callback) {
        this.errorCallback = callback;
        return this;
    }

    // ─── Execute ─────────────────────────────────────────────────────────────
    public Result<StreamSummary, StreamError> execute() {
        if (sink == null)
            throw new IllegalStateException("No sink configured. Call .sink() first.");

        AtomicLong bytesProcessed = new AtomicLong(0);
        AtomicLong chunksProcessed = new AtomicLong(0);
        long startTime = System.currentTimeMillis();

        try {
            sink.open();

            source.supply(rawChunk -> {
                // Apply filters
                for (Predicate<T> filter : filters) {
                    if (!filter.test(rawChunk)) return; // skip chunk
                }

                // Apply transformers in sequence
                T transformed = rawChunk;
                for (Function<T, T> transformer : transformers) {
                    transformed = transformer.apply(transformed);
                }

                // Write to sink
                try {
                    sink.write(transformed);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Track progress
                long chunkNum = chunksProcessed.incrementAndGet();
                long bytes = bytesProcessed.addAndGet(sizeOf(transformed));

                if (progressCallback != null) {
                    progressCallback.accept(new StreamProgress(bytes, chunkNum, source.totalBytes()));
                }
            });

            sink.close();

            StreamSummary summary = new StreamSummary(
                bytesProcessed.get(),
                chunksProcessed.get(),
                System.currentTimeMillis() - startTime,
                sink.destination()
            );

            if (completeCallback != null) completeCallback.accept(summary);
            return Result.ok(summary);

        } catch (Exception ex) {
            StreamError error = new StreamError(ex.getMessage(), bytesProcessed.get(), ex);
            sink.abort();
            if (errorCallback != null) errorCallback.accept(error);
            return Result.err(error);
        }
    }

    private long sizeOf(Object chunk) {
        if (chunk instanceof byte[])
            return ((byte[]) chunk).length;
        if (chunk instanceof String)
            return ((String) chunk).length();
        return 1;
    }

    // ─── Source abstraction ──────────────────────────────────────────────────

    public interface StreamSource<T> {
        void supply(Consumer<T> chunkConsumer) throws IOException;

        long totalBytes(); // -1 nếu không biết (streaming response)
    }

    @AllArgsConstructor
    static class InputStreamSource implements StreamSource<byte[]> {
        InputStream is;

        @Override
        public void supply(Consumer<byte[]> consumer) throws IOException {
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                consumer.accept(java.util.Arrays.copyOf(buf, read));
            }
        }

        @Override
        public long totalBytes() {
            try {
                return is.available();
            } catch (IOException e) {
                return -1;
            }
        }
    }

    @AllArgsConstructor
    static class LineSource implements StreamSource<String> {
        InputStream is;

        @Override
        public void supply(Consumer<String> consumer) throws IOException {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            }
        }

        @Override
        public long totalBytes() {
            return -1;
        }
    }

    // ─── Sink abstraction ────────────────────────────────────────────────────

    public interface StreamSink<T> {
        void open() throws IOException;

        void write(T chunk) throws IOException;

        void close() throws IOException;

        void abort(); // cleanup khi error

        String destination(); // mô tả đích (path, URL, ...)
    }

    // ─── Built-in Sinks ──────────────────────────────────────────────────────

    public static class Sinks {

        /**
         * Ghi ra file (append-safe, dùng BufferedOutputStream)
         */
        public static StreamSink<byte[]> toFile(Path path) {
            return new StreamSink<>() {
                private OutputStream out;

                @Override
                public void open() throws IOException {
                    Files.createDirectories(path.getParent());
                    out = new BufferedOutputStream(Files.newOutputStream(path,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE));
                }

                @Override
                public void write(byte[] chunk) throws IOException {
                    out.write(chunk);
                }

                @Override
                public void close() throws IOException {
                    if (out != null) out.close();
                }

                @Override
                public void abort() {
                    try {
                        if (out != null) out.close();
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                }

                @Override
                public String destination() {
                    return path.toString();
                }
            };
        }

        /**
         * Ghi CSV lines ra file
         */
        public static StreamSink<String> toTextFile(Path path) {
            return new StreamSink<>() {
                private BufferedWriter writer;

                @Override
                public void open() throws IOException {
                    Files.createDirectories(path.getParent());
                    writer = Files.newBufferedWriter(path,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }

                @Override
                public void write(String line) throws IOException {
                    writer.write(line);
                    writer.newLine();
                }

                @Override
                public void close() throws IOException {
                    if (writer != null) writer.close();
                }

                @Override
                public void abort() {
                    try {
                        if (writer != null) writer.close();
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                }

                @Override
                public String destination() {
                    return path.toString();
                }
            };
        }

        /**
         * SSE Sink: ghi từng chunk ra OutputStream của HTTP response.
         * Dùng với Spring: response.getOutputStream()
         */
        public static StreamSink<byte[]> toSse(OutputStream responseStream, BiConsumer<OutputStream, byte[]> formatter) {
            return new StreamSink<>() {
                @Override
                public void open() {
                }

                @Override
                public void write(byte[] chunk) throws IOException {
                    formatter.accept(responseStream, chunk);
                    responseStream.flush();
                }

                @Override
                public void close() {
                }

                @Override
                public void abort() {
                }

                @Override
                public String destination() {
                    return "SSE/HTTP stream";
                }
            };
        }

        /**
         * Collect sink: gom hết vào List (dùng cho test, hoặc data nhỏ).
         * Không dùng cho data thực sự lớn — sẽ OOM.
         */
        public static <T> CollectSink<T> collect() {
            return new CollectSink<>();
        }

        public static class CollectSink<T> implements StreamSink<T> {
            private final List<T> items = new ArrayList<>();

            @Override
            public void open() {
            }

            @Override
            public void write(T chunk) {
                items.add(chunk);
            }

            @Override
            public void close() {
            }

            @Override
            public void abort() {
                items.clear();
            }

            @Override
            public String destination() {
                return "in-memory list";
            }

            public List<T> getItems() {
                return List.copyOf(items);
            }
        }
    }

    // ─── Data classes ────────────────────────────────────────────────────────
    @Data
    @AllArgsConstructor
    public static class StreamProgress {
        long bytesProcessed;
        long chunksProcessed;
        long totalBytes;

        public double percentDone() {
            return totalBytes > 0 ? (double) bytesProcessed / totalBytes * 100 : -1;
        }
    }

    @Data
    @AllArgsConstructor
    public static class StreamSummary {
        long totalBytes;
        long totalChunks;
        long durationMs;
        String destination;
    }

    @Data
    @AllArgsConstructor
    public static class StreamError {
        String message;
        long bytesBeforeError;
        Throwable cause;
    }
}
