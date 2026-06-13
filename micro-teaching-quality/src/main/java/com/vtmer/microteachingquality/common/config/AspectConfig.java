package com.vtmer.microteachingquality.common.config;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.model.pojo.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    @Before("execution(* *..controller.*.*(..)) " +
            "&& !execution(* *..controller.UserController.login(..)) " +
            "&& !execution(* *..controller.UserController.logout(..))" +
            "&& !execution(* *..controller.UserController.createAccount(..))" +
            "&& !execution(* *..controller.UserController.sendCode(..))" +
            "&& !execution(* *..controller.UserController.sendCodeWithForgetting(..))" +
            "&& !execution(* *..controller.UserController.forgetPasswordAndReset(..))")
    public void beforeMethod(JoinPoint joinPoint) {
        User user = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        logger.info("用户 ： {}   调用接口： {}  ", user.getRealName(), joinPoint.getSignature());
        int i = 1;
        StringBuilder builder = new StringBuilder();
        for (Object o : joinPoint.getArgs()) {
            builder.append("参数 ").append(i).append("： ").append(o).append("   ");
            i++;
        }
        logger.info(builder.toString());
    }


    @AfterThrowing(value = "execution(* *..controller.*.*(..)) " +
            "&& !execution(* *..controller.UserController.login(..)) " +
            "&& !execution(* *..controller.UserController.logout(..) )" +
            "&& !execution(* *..controller.UserController.createAccount(..))" +
            "&& !execution(* *..controller.UserController.sendCode(..))" +
            "&& !execution(* *..controller.UserController.sendCodeWithForgetting(..))" +
            "&& !execution(* *..controller.UserController.forgetPasswordAndReset(..))", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        User user = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        logger.error("用户 ： {}   调用接口： {}   失败，出现异常：  {}", user.getRealName(), joinPoint.getSignature(), e.getMessage());
    }


    @After("execution(* *..controller.*.*(..)) " +
            "&& !execution(* *..controller.UserController.login(..)) " +
            "&& !execution(* *..controller.UserController.logout(..) )" +
            "&& !execution(* *..controller.UserController.createAccount(..))" +
            "&& !execution(* *..controller.UserController.sendCode(..))" +
            "&& !execution(* *..controller.UserController.sendCodeWithForgetting(..))" +
            "&& !execution(* *..controller.UserController.forgetPasswordAndReset(..))")
    public void afterMethod(JoinPoint joinPoint) {
        User user = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        logger.info("用户 ： {}   调用接口： {}   完成", user.getRealName(), joinPoint.getSignature());
    }
}
