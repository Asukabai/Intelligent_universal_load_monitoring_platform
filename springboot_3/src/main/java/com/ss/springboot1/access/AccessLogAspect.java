package com.ss.springboot1.access;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;




@Aspect
@Component
@Slf4j
public class AccessLogAspect {
    // 定义切点，这里切所有 Controller 的方法，可根据实际包名调整
    @Around("execution(* com.ss.springboot1.book.controller.*.*(..))")
    public Object logAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        log.info("访问 URL：{}，方法：{}，IP：{}", request.getRequestURL(), joinPoint.getSignature().getName(), request.getRemoteAddr());
        return joinPoint.proceed();
    }
}