package org.app.common.optimize;

public class FileProcessingService {

//    // Process large CSV file
//    public void processLargeCsv(Path csvPath) throws IOException {
//        try (Stream<String> lines = Files.lines(csvPath)) {
//            lines.parallel()                    // Parallel stream
//                .skip(1)                        // Skip header
//                .map(this::parseLine)
//                .filter(Objects::nonNull)
//                .forEach(this::processRecord);  // Process in parallel
//        }
//    }
//
//    // Batch processing với CompletableFuture
//    public CompletableFuture<Void> processBatch(List<File> files) {
//        List<CompletableFuture<Void>> futures = files.stream()
//            .map(file -> CompletableFuture.runAsync(
//                () -> processFile(file),
//                executor
//            ))
//            .toList();
//
//        return CompletableFuture.allOf(
//            futures.toArray(new CompletableFuture[0])
//        );
//    }
}
