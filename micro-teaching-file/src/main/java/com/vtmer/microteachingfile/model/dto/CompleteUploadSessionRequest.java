package com.vtmer.microteachingfile.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CompleteUploadSessionRequest {

    @NotNull
    private Integer operatorUserId;
}
