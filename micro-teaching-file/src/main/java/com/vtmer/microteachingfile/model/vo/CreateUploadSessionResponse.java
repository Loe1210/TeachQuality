package com.vtmer.microteachingfile.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateUploadSessionResponse {
    private Long sessionId;
    private String sessionCode;
    private Long chunkSize;
    private Integer totalChunks;
    private List<Integer> uploadedChunks;
    private Long existingFileObjectId;
}
