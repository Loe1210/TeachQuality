package com.vtmer.microteachingfile.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {
    private String rootDir;
    private String tempDir;
    private String objectDir;
    private String provider;
}
