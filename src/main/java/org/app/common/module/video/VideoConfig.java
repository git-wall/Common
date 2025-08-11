package org.app.common.module.video;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "video")
@Data
public class VideoConfig {

    private Storage storage = new Storage();
    private Streaming streaming = new Streaming();

    @Data
    public static class Storage {
        private String path = "uploads/videos/";
        private List<String> allowedExtensions = Arrays.asList("mp4", "avi", "mkv", "mov", "wmv");
        private long maxFileSize = 100 * 1024 * 1024; // 100MB
    }

    @Data
    public static class Streaming {
        private int chunkSize = 1024 * 1024; // 1MB
        private String cacheControl = "max-age=3600";
    }
}
