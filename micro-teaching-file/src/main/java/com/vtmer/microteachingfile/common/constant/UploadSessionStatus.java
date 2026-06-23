package com.vtmer.microteachingfile.common.constant;

public interface UploadSessionStatus {
    Integer INIT = 0;
    Integer UPLOADING = 1;
    Integer MERGING = 2;
    Integer COMPLETED = 3;
    Integer FAILED = 4;
    Integer CANCELED = 5;
    Integer EXPIRED = 6;
}
