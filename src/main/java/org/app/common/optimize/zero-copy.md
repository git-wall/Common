```java
@GetMapping("/large-file")
public ResponseEntity<StreamingResponseBody> downloadLargeFile() {
    StreamingResponseBody stream = outputStream -> {
        // Đọc và ghi từng chunk, không load hết vào memory
        try (InputStream input = new FileInputStream("large-file.zip")) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    };
    
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(stream);
}
// 1. Streaming database results (millions of rows)
@GetMapping("/export-users")
public ResponseEntity<StreamingResponseBody> exportUsers() {
    StreamingResponseBody stream = outputStream -> {
        try (Writer writer = new OutputStreamWriter(outputStream, UTF_8)) {
            userRepository.streamAll().forEach(user -> {
                writer.write(user.toCsv() + "\n");
            });
        }
    };
    
    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=users.csv")
            .body(stream);
}

// 2. Streaming video/audio
@GetMapping("/video/{id}")
public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable Long id) {
    Path videoPath = videoService.getVideoPath(id);
    
    StreamingResponseBody stream = outputStream -> {
        Files.copy(videoPath, outputStream);
    };
    
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("video/mp4"))
            .body(stream);
}

// 3. Real-time log streaming
@GetMapping("/logs/tail")
public ResponseEntity<StreamingResponseBody> tailLogs() {
    StreamingResponseBody stream = outputStream -> {
        try (BufferedReader reader = new BufferedReader(
                new FileReader("/var/log/app.log"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputStream.write((line + "\n").getBytes());
                outputStream.flush(); // Push ngay cho client
            }
        }
    };
    
    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(stream);
}

// Cách 1: Dùng Resource (Spring tự động optimize)
@GetMapping("/file/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
  Path path = Paths.get("/files/" + filename);
  Resource resource = new FileSystemResource(path);

  return ResponseEntity.ok()
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .contentLength(resource.contentLength())
    .body(resource); // Spring dùng zero-copy nếu có thể
}

// Cách 2: Dùng FileChannel (manual zero-copy)
@GetMapping("/zero-copy/{filename}")
public void downloadFileZeroCopy(@PathVariable String filename,
                                 HttpServletResponse response) throws IOException {
  Path path = Paths.get("/files/" + filename);

  response.setContentType("application/octet-stream");
  response.setContentLengthLong(Files.size(path));

  try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
       WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {

    // Zero-copy transfer
    fileChannel.transferTo(0, fileChannel.size(), outputChannel);
  }
}

// Cách 3: Dùng ResourceRegion (hỗ trợ Range requests - cho video streaming)
@GetMapping("/video-range/{id}")
public ResponseEntity<ResourceRegion> streamVideoWithRange(
  @PathVariable Long id,
  @RequestHeader HttpHeaders headers) throws IOException {

  Resource video = new FileSystemResource("/videos/" + id + ".mp4");
  long contentLength = video.contentLength();

  ResourceRegion region;
  List<HttpRange> ranges = headers.getRange();

  if (!ranges.isEmpty()) {
    HttpRange range = ranges.get(0);
    long start = range.getRangeStart(contentLength);
    long end = range.getRangeEnd(contentLength);
    region = new ResourceRegion(video, start, end - start + 1);
  } else {
    region = new ResourceRegion(video, 0, contentLength);
  }

  return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
    .contentType(MediaType.parseMediaType("video/mp4"))
    .body(region);
}
```

## 2. Zero-Copy - Tăng throughput cao hơn

**Zero-copy** là kỹ thuật transfer data mà KHÔNG copy qua user space (JVM heap).

### Cách thông thường (có copy):
```
Disk → Kernel buffer → JVM heap → Kernel buffer → Network
       (copy 1)        (copy 2)    (copy 3)
```

### Zero-copy (không qua JVM):
```
Disk → Kernel buffer → Network
       (sendfile syscall)
```


```java
// Download file qua network (zero-copy)
@GetMapping("/download-optimized/{filename}")
public void downloadOptimized(@PathVariable String filename,
                              HttpServletResponse response) throws IOException {
  Path path = Paths.get("/files/" + filename);

  response.setContentType("application/octet-stream");
  response.setContentLengthLong(Files.size(path));
  response.setHeader("Content-Disposition",
    "attachment; filename=" + filename);

  try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
       WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {

    long position = 0;
    long size = fileChannel.size();

    while (position < size) {
      position += fileChannel.transferTo(position, size - position, outputChannel);
    }
  }
}
// Video Streaming với Range Support
@GetMapping("/stream-video/{id}")
public void streamVideo(@PathVariable Long id,
                        @RequestHeader(value = "Range", required = false) String range,
                        HttpServletResponse response) throws IOException {
    
    Path videoPath = Paths.get("/videos/" + id + ".mp4");
    long fileSize = Files.size(videoPath);
    
    long start = 0;
    long end = fileSize - 1;
    
    // Parse Range header: "bytes=1024-2047"
    if (range != null && range.startsWith("bytes=")) {
        String[] ranges = range.substring(6).split("-");
        start = Long.parseLong(ranges[0]);
        if (ranges.length > 1 && !ranges[1].isEmpty()) {
            end = Long.parseLong(ranges[1]);
        }
    }
    
    long contentLength = end - start + 1;
    
    response.setStatus(range != null ? 206 : 200); // 206 Partial Content
    response.setContentType("video/mp4");
    response.setContentLengthLong(contentLength);
    response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
    response.setHeader("Accept-Ranges", "bytes");
    
    try (FileChannel channel = FileChannel.open(videoPath, StandardOpenOption.READ);
         WritableByteChannel out = Channels.newChannel(response.getOutputStream())) {
        
        channel.transferTo(start, contentLength, out);
    }
}
// Large Log File Reader (paging)
@RestController
public class LogController {

  @GetMapping("/logs")
  public ResponseEntity<String> getLogs(
    @RequestParam(defaultValue = "0") long offset,
    @RequestParam(defaultValue = "1000") int limit) throws IOException {

    Path logPath = Paths.get("/var/log/app.log");

    try (FileChannel channel = FileChannel.open(logPath, StandardOpenOption.READ)) {
      long size = channel.size();

      if (offset >= size) {
        return ResponseEntity.ok("No more logs");
      }

      long readSize = Math.min(limit, size - offset);
      ByteBuffer buffer = ByteBuffer.allocate((int) readSize);

      channel.read(buffer, offset);
      buffer.flip();

      String logs = new String(buffer.array(), 0, buffer.limit());

      HttpHeaders headers = new HttpHeaders();
      headers.set("X-Total-Size", String.valueOf(size));
      headers.set("X-Next-Offset", String.valueOf(offset + readSize));

      return ResponseEntity.ok().headers(headers).body(logs);
    }
  }
}
// High-Performance File Upload
@PostMapping("/upload-optimized")
public ResponseEntity<String> uploadOptimized(@RequestParam("file") MultipartFile file)
  throws IOException {

  Path tempFile = Files.createTempFile("upload-", ".tmp");
  Path finalPath = Paths.get("/uploads/" + file.getOriginalFilename());

  // Ghi vào temp file
  try (FileChannel destChannel = FileChannel.open(tempFile,
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE);
       ReadableByteChannel srcChannel = Channels.newChannel(file.getInputStream())) {

    destChannel.transferFrom(srcChannel, 0, Long.MAX_VALUE);
  }

  // Validate file...

  // Move atomically
  Files.move(tempFile, finalPath, StandardCopyOption.ATOMIC_MOVE);

  return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
}
```
### Benchmark: Copy 1GB file
```java
public class FileChannelBenchmark {
    
    // 1. Traditional way: ~15 seconds
    public void copyTraditional(String src, String dest) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    // 2. FileChannel: ~5 seconds
    public void copyFileChannel(String src, String dest) throws IOException {
        try (FileChannel srcChannel = FileChannel.open(Paths.get(src), 
                                                       StandardOpenOption.READ);
             FileChannel destChannel = FileChannel.open(Paths.get(dest), 
                                                        StandardOpenOption.WRITE,
                                                        StandardOpenOption.CREATE)) {
            
            srcChannel.transferTo(0, srcChannel.size(), destChannel);
        }
    }
    
    // 3. Memory-mapped: ~2 seconds
    public void copyMemoryMapped(String src, String dest) throws IOException {
        try (FileChannel srcChannel = FileChannel.open(Paths.get(src), 
                                                       StandardOpenOption.READ);
             FileChannel destChannel = FileChannel.open(Paths.get(dest), 
                                                        StandardOpenOption.WRITE,
                                                        StandardOpenOption.CREATE)) {
            
            long size = srcChannel.size();
            MappedByteBuffer srcBuffer = srcChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            MappedByteBuffer destBuffer = destChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            
            destBuffer.put(srcBuffer);
        }
    }
}
```
Khi nào dùng FileChannel?

✅ File lớn (>100MB)
✅ Cần random access
✅ Cần performance cao nhất
✅ Video/audio streaming
✅ Database file handling
✅ Shared memory giữa processes