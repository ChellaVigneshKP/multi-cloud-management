package com.multicloud.auth.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoginExceptionAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoginExceptionAspect.class);

    @AfterThrowing(
            pointcut = "execution(* com.multicloud.auth.service.auth.LoginService.*(..))",
            throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String methodName = joinPoint.getSignature().toShortString();
        String message = ex.getMessage();
        logger.error("{} occurred in {} because of: {}", exceptionName, methodName, message);
    }
}