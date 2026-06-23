package com.vtmer.microteachingfile.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateUploadSessionRequest {

    @NotBlank
    private String bizType;

    @NotNull
    @Min(1)
    private Long bizId;

    @NotBlank
    private String originalName;

    @NotNull
    @Min(1)
    private Long expectedSize;

    @Min(1)
    private Long chunkSize;

    @NotNull
    @Min(1)
    private Integer totalChunks;

    private String fileHash;

    @NotBlank
    private String contentType;

    @NotNull
    @Min(1)
    private Integer createUserId;
}
