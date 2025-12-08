package com.robsartin.graphs.application;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging entry and exit of public Controller methods.
 * Logs method name, arguments on entry, and return value with elapsed time on exit.
 */
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    /**
     * Pointcut that matches all public methods in classes annotated with @RestController
     * within the application package.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
    }

    /**
     * Pointcut that matches all public methods.
     */
    @Pointcut("execution(public * *(..))")
    public void publicMethods() {
    }

    /**
     * Around advice that logs method entry and exit for all public controller methods.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the method throws an exception
     */
    @Around("restControllerMethods() && publicMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("ENTRY: {}.{}() with arguments: {}", className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;

            log.info("EXIT: {}.{}() returned: {} | elapsed time: {} ms",
                    className, methodName, result, elapsedTime);

            return result;
        } catch (Throwable ex) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            log.error("EXCEPTION: {}.{}() threw: {} | elapsed time: {} ms",
                    className, methodName, ex.getClass().getSimpleName() + ": " + ex.getMessage(), elapsedTime);

            throw ex;
        }
    }
}
