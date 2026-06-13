package com.vtmer.microteachingquality.common.exception;

/**
 * @author 墨小小
 * @date 21-09-24 21:23
 */
public class CustomException extends RuntimeException {
    public CustomException() {
        super();
    }

    public CustomException(String message) {
        super(message);
    }
}
