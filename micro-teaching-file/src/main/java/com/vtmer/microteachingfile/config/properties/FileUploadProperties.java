package com.vtmer.microteachingfile.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    private List<String> allowedExtensions = new ArrayList<>();
    private Long maxFileSizeBytes;
    private Long defaultChunkSizeBytes;
    private Integer maxConcurrentSessionsPerUser;
    private Integer sessionExpireMinutes;
    private Integer maxCompleteRetryCount;
}
