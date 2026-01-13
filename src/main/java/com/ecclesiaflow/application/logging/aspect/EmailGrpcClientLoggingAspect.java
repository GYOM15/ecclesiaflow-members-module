package com.ecclesiaflow.application.logging.aspect;

import com.ecclesiaflow.application.logging.SecurityMaskingUtils;
import com.ecclesiaflow.business.exceptions.EmailServiceException;
import com.ecclesiaflow.business.exceptions.EmailServiceUnavailableException;
import com.ecclesiaflow.business.exceptions.GrpcCommunicationException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AOP logging aspect for Email gRPC client with circuit breaker support.
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class EmailGrpcClientLoggingAspect {

    @Pointcut("execution(* com.ecclesiaflow.io.communication.email.EmailGrpcClient.*(..))")
    public void emailGrpcClientCalls() {}

    @Before("emailGrpcClientCalls()")
    public void logBeforeEmailRpcCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.debug("GRPC-EMAIL: Calling EmailService.{}", methodName);
    }

    @AfterReturning(pointcut = "emailGrpcClientCalls()", returning = "result")
    public void logAfterSuccessfulEmailRpcCall(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String maskedResult = result != null ? SecurityMaskingUtils.maskId(result) : "void";
        log.info("GRPC-EMAIL: EmailService.{} completed - result={}", methodName, maskedResult);
    }

    @AfterThrowing(pointcut = "emailGrpcClientCalls()", throwing = "exception")
    public void logEmailRpcCallError(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();

        if (exception instanceof CallNotPermittedException) {
            log.warn("GRPC-EMAIL: Circuit breaker OPEN for EmailService.{}", methodName);
        } else if (exception instanceof EmailServiceUnavailableException) {
            log.error("GRPC-EMAIL: Service unavailable during {} - fallback triggered", methodName);
        } else if (exception instanceof EmailServiceException ese) {
            log.error("GRPC-EMAIL: Failed {} for {} - operation={}", 
                    methodName, 
                    ese.getMaskedEmailAddress(),
                    ese.getOperation());
        } else if (exception instanceof GrpcCommunicationException gce) {
            log.error("GRPC-EMAIL: Communication error during {} - status={}, desc={}", 
                    methodName,
                    gce.getStatusCode(),
                    gce.getSanitizedDescription());
        } else {
            log.error("GRPC-EMAIL: Error during {} - {}: {}", 
                    methodName,
                    exception.getClass().getSimpleName(),
                    SecurityMaskingUtils.rootMessage(exception));
        }
    }
}
