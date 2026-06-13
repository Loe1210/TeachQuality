package com.vtmer.microteachingquality.common.handler;

import com.vtmer.microteachingquality.common.ResponseMessage;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * @author Hung
 * @date 2022/7/9 20:07
 */
@RestControllerAdvice
public class ValidExceptionHandler {

    @ExceptionHandler({BindException.class})
    public ResponseMessage<String> validExceptionHandler(BindException exception) {
        return ResponseMessage.newErrorInstance(exception.getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseMessage<String> validExceptionHandler(ConstraintViolationException exception) {
        return ResponseMessage.newErrorInstance(exception.getMessage().split(" ")[1]);
    }

}
