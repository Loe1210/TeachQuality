package com.vtmer.microteachingmessage.handler;

import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingmessage.exception.IllegalMessageTagException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Hung
 * @date 2022/7/12 10:56
 */
@ControllerAdvice
public class IllegalMessageTagExceptionHandler {

    @ExceptionHandler({IllegalMessageTagException.class})
    public Result<String> illegalMessageTagExceptionHandler(IllegalMessageTagException exception) {
        return Result.failed(exception.getMessage());
    }
}
