package com.vtmer.microteachingfile.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UploadSessionDetailResponse {
    private Long sessionId;
    private String sessionCode;
    private String bizType;
    private Long bizId;
    private String originalName;
    private Long expectedSize;
    private Long chunkSize;
    private Integer totalChunks;
    private Integer uploadedChunks;
    private Integer status;
    private List<Integer> uploadedChunkIndexes;
    private LocalDateTime expireTime;
    private Long fileObjectId;
}
