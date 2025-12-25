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
 * Sensitive data (passwords, tokens, credentials) is filtered from logs.
 */
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingAspect.class);
    private static final java.util.Set<String> SENSITIVE_PARAM_NAMES = java.util.Set.of(
            "password", "secret", "token", "credential", "key", "authorization"
    );

    /**
     * Pointcut that matches all public methods in classes annotated with @RestController
     * within the application package.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
    }

    /**
     * Pointcut that matches all public methods in classes annotated with @Controller
     * within the application package.
     */
    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerMethods() {
    }

    /**
     * Pointcut that matches all public methods.
     */
    @Pointcut("execution(public * *(..))")
    public void publicMethods() {
    }

    /**
     * Around advice that logs method entry and exit for all public controller methods.
     * Filters sensitive data from log output.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the method throws an exception
     */
    @Around("(restControllerMethods() || controllerMethods()) && publicMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String safeArgs = sanitizeArguments(args, joinPoint);
        log.info("ENTRY: {}.{}() with arguments: {}", className, methodName, safeArgs);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;

            String safeResult = sanitizeResult(result);
            log.info("EXIT: {}.{}() returned: {} | elapsed time: {} ms",
                    className, methodName, safeResult, elapsedTime);

            return result;
        } catch (Throwable ex) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            log.error("EXCEPTION: {}.{}() threw: {} | elapsed time: {} ms",
                    className, methodName, ex.getClass().getSimpleName() + ": " + ex.getMessage(), elapsedTime);

            throw ex;
        }
    }

    /**
     * Sanitizes method arguments to prevent logging sensitive data.
     */
    private String sanitizeArguments(Object[] args, ProceedingJoinPoint joinPoint) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        String[] paramNames = getParameterNames(joinPoint);
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            String paramName = (paramNames != null && i < paramNames.length) ? paramNames[i] : "";
            if (isSensitiveParameter(paramName) || isSensitiveValue(args[i])) {
                sb.append("[REDACTED]");
            } else if (args[i] == null) {
                sb.append("null");
            } else {
                sb.append(args[i].toString());
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Gets parameter names from the join point if available.
     */
    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        try {
            org.aspectj.lang.reflect.MethodSignature signature =
                    (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            return signature.getParameterNames();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a parameter name indicates sensitive data.
     */
    private boolean isSensitiveParameter(String paramName) {
        if (paramName == null) {
            return false;
        }
        String lowerName = paramName.toLowerCase();
        return SENSITIVE_PARAM_NAMES.stream().anyMatch(lowerName::contains);
    }

    /**
     * Checks if a value appears to be sensitive.
     */
    private boolean isSensitiveValue(Object value) {
        if (value == null) {
            return false;
        }
        String className = value.getClass().getSimpleName().toLowerCase();
        return className.contains("credential") ||
               className.contains("password") ||
               className.contains("token") ||
               className.contains("authentication");
    }

    /**
     * Sanitizes return values to prevent logging sensitive data.
     */
    private String sanitizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        if (isSensitiveValue(result)) {
            return "[REDACTED]";
        }
        return result.toString();
    }
}
