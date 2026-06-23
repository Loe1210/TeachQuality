package com.vtmer.microteachingfile.common.constant;

public interface FileObjectStatus {
    Integer INIT = 0;
    Integer UPLOADING = 1;
    Integer MERGING = 2;
    Integer READY = 3;
    Integer FAILED = 4;
    Integer DELETED = 5;
}
