package com.vtmer.microteachingfile.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileObjectResponse {
    private Long id;
    private String bizType;
    private Long bizId;
    private String originalName;
    private String storagePath;
    private String storageProvider;
    private Long fileSize;
    private String contentType;
    private String fileHash;
    private Integer status;
}
