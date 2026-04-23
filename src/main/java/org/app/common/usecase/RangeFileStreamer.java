package org.app.common.usecase;

import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class RangeFileStreamer {

    private static final int BUFFER_SIZE = 8192;

    /**
     * <pre>{@code
     * @RestController
     * @RequestMapping("/video")
     * public class VideoController {
     *
     *     @GetMapping("/{name}")
     *     public void streamVideo(@PathVariable String name,
     *                             HttpServletRequest request,
     *                             HttpServletResponse response) {
     *
     *         Path path = Paths.get("videos", name);
     *         response.setHeader("Cache-Control", "public, max-age=3600");
     *         response.setContentType("video/mp4");
     *
     *         RangeFileStreamer.stream(path, request, response);
     *     }
     * }
     * }</pre>
     * */

    public static void stream(Path path, HttpServletRequest request, HttpServletResponse response) {
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {

            long fileSize = fc.size();
            String rangeHeader = request.getHeader("Range");

            if (rangeHeader == null) {
                // full content
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Content-Length", String.valueOf(fileSize));

                try (OutputStream os = response.getOutputStream()) {
                    fc.transferTo(0, fileSize, Channels.newChannel(os));
                }
                return;
            }

            // Parse range
            long start = 0;
            long end = fileSize - 1;

            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            if (ranges.length > 0 && !ranges[0].isEmpty()) {
                start = Long.parseLong(ranges[0]);
            }
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }

            if (end >= fileSize) {
                end = fileSize - 1;
            }

            long contentLength = end - start + 1;

            // Headers
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));
            response.setHeader("Content-Length", String.valueOf(contentLength));

            try (OutputStream os = response.getOutputStream()) {
                fc.transferTo(start, contentLength, Channels.newChannel(os));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
