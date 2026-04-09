package com.retailstore.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Aspect
@Component
public class GenericServiceLoggingAspect {

    @Pointcut("within(com.retailstore..service..*)")
    public void allServiceMethods() {}

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "pwd",
            "token", "accessToken", "refreshToken", "jwt",
            "secret", "secretKey"
    ));

    private boolean isSensitiveModule(String className){
        return className.contains("Auth")
                || className.contains("RefreshToken")
                || className.contains("User")
                || className.contains("Address");
    }

    private Object makeSensitive(Object arg){
        if (arg == null) return null;

        if (arg instanceof String str){
            String lower = str.toLowerCase();
            for (String sensitive: SENSITIVE_FIELDS){
                if (lower.contains(sensitive.toLowerCase())){
                    return "[MAKSED]";
                }
            }
            return arg;
        }

        if (isPrimitiveOrWrapper(arg.getClass())) return arg;

        try{
            Object clone = cloneObject(arg);
            Field[] fields = clone.getClass().getDeclaredFields();

            for (Field field: fields){
                field.setAccessible(true);
                Object value = field.get(clone);

                if (value == null) continue;

                if (isSensitiveField(field.getName())){
                    field.set(clone, "[MASKED]");
                }
            }
            return clone;
        }catch (Exception e){
            return arg;
        }
    }

    private boolean isSensitiveField(String fieldName){
        return SENSITIVE_FIELDS.stream()
                .anyMatch(s -> fieldName.toLowerCase().contains(s.toLowerCase()));
    }

    private boolean isPrimitiveOrWrapper(Class<?> type){
        return type.isPrimitive()
                || type == Integer.class
                || type == Long.class
                || type == Boolean.class
                || type == Double.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == Character.class;
    }

    private Object cloneObject(Object obj) throws Exception{
        Object clone = obj.getClass().getDeclaredConstructor().newInstance();
        for (Field field: obj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            field.set(clone, field.get(obj));
        }
        return clone;
    }

    //======================< ADVICES >=====================
    @Before("allServiceMethods()")
    public void logBefore(JoinPoint joinPoint){
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[{}] START -> {}()", className, methodName);

        if (!isSensitiveModule(className)){
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0){
                Object[] maskedArgs = Arrays.stream(args)
                                .map(this::makeSensitive)
                                        .toArray();
                log.debug("[{}} Arguments -> {}", className, Arrays.toString(maskedArgs));
            }
        }else{
            log.debug("[{}] Arguments logging skipped (sensitive)", className);
        }
    }

    @AfterReturning(value = "allServiceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result){
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[{}] END -> {}() | Returned: {}", className, methodName,
                result != null ? result.getClass().getSimpleName() : "null");
    }

    @AfterThrowing(value = "allServiceMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception){
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[{}] ERROR -> {}()", className, methodName, exception);
    }

    @Around("allServiceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();

        try{
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.debug("[{}] Execution Time -> {}() took {} ms", className, methodName, duration);

            return result;

        }catch (Exception exception){
            long duration = System.currentTimeMillis() - start;

            log.error("[{}] Execution Time (FAILED) -> {}() thew after {} ms", className, methodName, duration);

            throw exception;
        }
    }
}
