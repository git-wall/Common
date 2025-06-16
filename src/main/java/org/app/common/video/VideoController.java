package org.app.common.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.SpringContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final VideoRepository videoRepository;

    // Upload video and return ID
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + "." + extension;

            // Create storage directory if not exists
            Path storagePath = Paths.get(SpringContext.getContext()
                    .getEnvironment()
                    .getProperty("video.storage.path", "uploads/videos/"));
            Files.createDirectories(storagePath);

            // Save file to disk
            Path filePath = storagePath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save to database
            Video video = Video.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(filePath.toString())
                    .build();

            video = videoRepository.save(video);

            // Return video ID
            Map<String, Object> response = new HashMap<>();
            response.put("id", video.getId());
            response.put("originalFilename", video.getOriginalFilename());
            response.put("size", video.getFileSize());
            response.put("message", "Video uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading video: " + e.getMessage());
        }
    }

    // Stream video by ID with range support (for skip/seek)
    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            // Find video by ID
            Optional<Video> videoOpt = videoRepository.findById(id);
            if (videoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Video video = videoOpt.get();
            File videoFile = new File(video.getFilePath());

            // Check if file exists on disk
            if (!videoFile.exists()) {
                log.error("Video file not found: {}", video.getFilePath());
                return ResponseEntity.notFound().build();
            }

            long fileSize = videoFile.length();

            // Handle range requests (for video skip/seek)
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(videoFile, rangeHeader, fileSize, video.getContentType());
            }

            // Return full video
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(video.getContentType()))
                    .contentLength(fileSize)
                    .header("Accept-Ranges", "bytes")
                    .body(new FileSystemResource(videoFile));

        } catch (Exception e) {
            log.error("Error streaming video ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Handle range requests for video seeking
    private ResponseEntity<Resource> handleRangeRequest(File videoFile,
                                                        String rangeHeader,
                                                        long fileSize,
                                                        String contentType) {
        try {
            // Parse range header: "bytes=start-end"
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty()
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            // Validate range
            if (end >= fileSize || start > end) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header("Content-Range", "bytes */" + fileSize)
                        .build();
            }

            long contentLength = end - start + 1;

            // Create partial resource for streaming
            PartialFileResource partialResource = new PartialFileResource(videoFile, start, contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(contentLength)
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Range", "bytes " + start + "-" + end + "/" + fileSize)
                    .body(partialResource);

        } catch (Exception e) {
            log.error("Error handling range request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
