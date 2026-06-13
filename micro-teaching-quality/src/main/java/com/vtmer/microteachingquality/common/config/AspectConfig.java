package com.vtmer.microteachingquality.common.config;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.model.pojo.User;
import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


/**
 * @author 墨小小
 * @date 21-09-25 14:28
 */
@Aspect
@Component
public class AspectConfig {

    private static final Logger logger = LoggerFactory.getLogger(AspectConfig.class);
    private static final String CONTROLLER_POINTCUT = "execution(* *..controller.*.*(..)) " +
            "&& !execution(* *..controller.UserController.login(..)) " +
            "&& !execution(* *..controller.UserController.createAccount(..))" +
            "&& !execution(* *..controller.UserController.sendCode(..))" +
            "&& !execution(* *..controller.UserController.sendCodeWithForgetting(..))" +
            "&& !execution(* *..controller.UserController.forgetPasswordAndReset(..))";


    @Before(CONTROLLER_POINTCUT)
    public void beforeMethod(JoinPoint joinPoint) {
        logger.info("用户 ： {}   调用接口： {}", resolveCurrentUsername(), buildSignature(joinPoint));
        logger.info("接口参数： {}", buildArguments(joinPoint));
    }


    @AfterThrowing(value = CONTROLLER_POINTCUT, throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        logger.error("用户 ： {}   调用接口： {}   失败，出现异常： {}",
                resolveCurrentUsername(),
                buildSignature(joinPoint),
                e.getMessage());
    }


    @After(CONTROLLER_POINTCUT)
    public void afterMethod(JoinPoint joinPoint) {
        logger.info("用户 ： {}   调用接口： {}   完成", resolveCurrentUsername(), buildSignature(joinPoint));
    }

    private String resolveCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getRealName();
        }
        try {
            User user = JSON.parseObject(JSONUtil.toJsonStr(principal), User.class);
            return user != null && user.getRealName() != null ? user.getRealName() : String.valueOf(principal);
        } catch (Exception ignored) {
            return String.valueOf(principal);
        }
    }

    private String buildSignature(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        return signature == null ? "unknown" : signature.toShortString();
    }

    private String buildArguments(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "[]";
        }
        CodeSignature signature = (CodeSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            String parameterName = parameterNames != null && parameterNames.length > i ? parameterNames[i] : "arg" + i;
            builder.append(parameterName).append('=').append(maskSensitiveValue(args[i]));
        }
        return builder.append(']').toString();
    }

    private String maskSensitiveValue(Object value) {
        if (value == null) {
            return "null";
        }
        String text = String.valueOf(value);
        String masked = text
                .replaceAll("(?i)(password|pwd|secret|token|authorization)=([^,}\\]]+)", "$1=***")
                .replaceAll("(?i)\"(password|pwd|secret|token|authorization)\":\"[^\"]*\"", "\"$1\":\"***\"");
        return masked.length() > 500 ? masked.substring(0, 500) + "..." : masked;
    }
}
