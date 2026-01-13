package com.ecclesiaflow.application.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP dédié au logging des opérations gRPC serveur du module Members.
 * <p>
 * Cette classe implémente un aspect spécialisé dans le logging des communications
 * gRPC entrantes sur le module Members. Elle capture les appels RPC depuis d'autres
 * modules (Auth, etc.), les erreurs de traitement et les métriques de performance.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Aspect infrastructure - Audit des communications gRPC</p>
 *
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Logging des démarrages/arrêts du serveur gRPC Members</li>
 *   <li>Audit des appels RPC entrants (GetMemberConfirmationStatus, CheckMemberExists)</li>
 *   <li>Capture des erreurs de traitement gRPC</li>
 *   <li>Traçabilité inter-modules (Auth → Members)</li>
 * </ul>
 * 
 * <p><strong>Note :</strong> Le module Members expose UNIQUEMENT des services Members via gRPC.
 * L'envoi d'emails est délégué à un module Email externe via {@code EmailGrpcClient}.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class GrpcServerLoggingAspect {

    // ========================================================================
    // Pointcuts - Serveur gRPC
    // ========================================================================

    @Pointcut("execution(* com.ecclesiaflow.io.grpc.server.GrpcServerConfig.start())")
    public void grpcServerStart() {}

    @Pointcut("execution(* com.ecclesiaflow.io.grpc.server.GrpcServerConfig.stop())")
    public void grpcServerStop() {}

    @Pointcut("execution(* com.ecclesiaflow.io.grpc.server.MembersGrpcServiceImpl.*(..))")
    public void grpcServiceCalls() {}

    // ========================================================================
    // Advices - Démarrage/Arrêt
    // ========================================================================

    @Before("grpcServerStart()")
    public void logBeforeServerStart(JoinPoint joinPoint) {
        log.info("🚀 GRPC: Initializing gRPC server for Members module...");
    }

    @AfterReturning("grpcServerStart()")
    public void logAfterServerStart(JoinPoint joinPoint) {
        log.info("✅ GRPC: gRPC server started successfully and ready to accept connections");
    }

    @AfterThrowing(pointcut = "grpcServerStart()", throwing = "exception")
    public void logServerStartError(JoinPoint joinPoint, Exception exception) {
        log.error("❌ GRPC: Failed to start gRPC server - {}: {}", 
                exception.getClass().getSimpleName(), 
                exception.getMessage());
    }

    @Before("grpcServerStop()")
    public void logBeforeServerStop(JoinPoint joinPoint) {
        log.info("🛑 GRPC: Initiating graceful shutdown of gRPC server...");
    }

    @AfterReturning("grpcServerStop()")
    public void logAfterServerStop(JoinPoint joinPoint) {
        log.info("✅ GRPC: gRPC server stopped successfully");
    }

    // ========================================================================
    // Advices - Appels RPC entrants
    // ========================================================================

    @Before("grpcServiceCalls()")
    public void logBeforeRpcCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        
        if ("getMemberConfirmationStatus".equals(methodName)) {
            log.info("📨 GRPC-RPC: Received {} request from Auth module", methodName);
        } else {
            log.debug("📡 GRPC-RPC: Received {} request", methodName);
        }
    }

    @AfterReturning("grpcServiceCalls()")
    public void logAfterSuccessfulRpcCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        
        if ("getMemberConfirmationStatus".equals(methodName)) {
            log.info("✅ GRPC-RPC: {} completed successfully", methodName);
        } else {
            log.debug("✅ GRPC-RPC: {} completed successfully", methodName);
        }
    }

    @AfterThrowing(pointcut = "grpcServiceCalls()", throwing = "exception")
    public void logRpcCallError(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        
        if (exception instanceof IllegalArgumentException) {
            log.warn("⚠️  GRPC-RPC: Invalid argument in {} - {}", 
                    methodName, 
                    exception.getMessage());
        } else {
            log.error("❌ GRPC-RPC: Error in {} - {}: {}", 
                    methodName,
                    exception.getClass().getSimpleName(), 
                    exception.getMessage());
        }
    }
}
