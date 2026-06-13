package com.vtmer.microteachingquality.common.handler;

import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.common.exception.base.BaseException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Hung
 * @date 2022/7/9 20:35
 */
@RestControllerAdvice
public class CustomExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(CustomException.class)
    public ResponseMessage<String> customExceptionHandler(CustomException exception) {
        return ResponseMessage.newErrorInstance(exception.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public ResponseMessage<String> baseExceptionHandler(BaseException exception) {
        return ResponseMessage.newErrorInstance(exception.getMessage());
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseMessage<String> fileSizeLimitExceededExceptionHandler(FileSizeLimitExceededException exception) {
        return ResponseMessage.newErrorInstance("文件大小超出限制" + maxFileSize);
    }
}
