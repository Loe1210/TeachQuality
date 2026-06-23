package com.vtmer.microteachingfile.common.handler;

import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingfile.common.exception.CustomException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Result<String> handleCustomException(CustomException exception) {
        return Result.failed(exception.getMessage());
    }
}
