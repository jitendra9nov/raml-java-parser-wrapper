package com.bhadouriya.raml.oauth;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Aspect
@Component
public class AspectLogging {
    private static final Logger LOGGER = Logger.getLogger(AspectLogging.class.getName());

    @Pointcut("execution(* com.bhadouriya.raml..*.*(..))")
    private void codeOps() {

    }

    @Around("codeOps()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(Level.WARNING,

                    String.format("Entering in the method :%s.%s() with arguments[s]= %s", joinPoint.getSignature().getDeclaringTypeName(),//package
                            joinPoint.getSignature().getName(),//method
                            Arrays.toString(joinPoint.getArgs()))
            );
        }
        try {
            Object result = joinPoint.proceed();
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING,
                        String.format("Exiting from the method :%s.%s() with arguments[s]= %s", joinPoint.getSignature().getDeclaringTypeName(),//package
                                joinPoint.getSignature().toShortString(),//method
                                result)
                );
            }
            return result;

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE,
                    String.format("Illegal arguments: %s in %s.%s()",
                            Arrays.toString(joinPoint.getArgs()),
                            joinPoint.getSignature().getDeclaringTypeName(),//package
                            joinPoint.getSignature().getName()//method
                    )
            );
            throw e;
        }
    }

    @AfterThrowing(value = "codeOps()", throwing = "throwable")
    public void handleException(JoinPoint joinPoint, Throwable throwable) throws Throwable {
        LOGGER.log(Level.SEVERE,
                String.format("Exception occurred in the method: %s.%s() with exception message : %s",
                        joinPoint.getSignature().getDeclaringTypeName(),//package
                        joinPoint.getSignature().getName(),//method
                        throwable.getMessage()
                )
        );
        if (!ObjectUtils.isEmpty(throwable.getSuppressed())) {
            for (Throwable th : throwable.getSuppressed()
            ) {
                LOGGER.log(Level.SEVERE,
                        String.format("Suppressed Exception method: %s.%s() with exception message : %s",
                                joinPoint.getSignature().getDeclaringTypeName(),//package
                                joinPoint.getSignature().getName(),//method
                                th.getMessage()
                        )
                );
            }
        }
    }
}
