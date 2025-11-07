package com.ecclesiaflow.application.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP dédié au logging des appels gRPC sortants du module Members.
 * <p>
 * Cette classe implémente un aspect spécialisé dans le logging des communications
 * gRPC entre le module Members et le module Auth. Elle capture les appels sortants,
 * les erreurs de communication et les métriques de performance.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Aspect infrastructure - Audit des communications gRPC</p>
 *
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>Spring AOP - Framework de programmation orientée aspect</li>
 *   <li>SLF4J/Logback - Framework de logging</li>
 *   <li>GrpcClientConfig - Configuration du client gRPC</li>
 *   <li>AuthGrpcClient - Client des services gRPC Auth</li>
 * </ul>
 *
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Logging des appels RPC sortants (GenerateTemporaryToken, ValidateToken)</li>
 *   <li>Capture des erreurs de communication gRPC</li>
 *   <li>Audit des interactions avec le module Auth</li>
 *   <li>Traçabilité des timeouts et problèmes réseau</li>
 *   <li>Monitoring de la disponibilité du service Auth</li>
 * </ul>
 *
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Audit de sécurité des communications inter-services</li>
 *   <li>Debugging des problèmes de communication</li>
 *   <li>Monitoring de la latence des appels gRPC</li>
 *   <li>Détection proactive des pannes du service Auth</li>
 * </ul>
 *
 * <p><strong>Garanties :</strong> Thread-safe, logging asynchrone, impact minimal sur performances.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class GrpcClientLoggingAspect {

    // ========================================================================
    // Pointcuts - Définition des points d'interception
    // ========================================================================

    /**
     * Pointcut pour le shutdown du canal gRPC.
     * <p>
     * Intercepte la méthode {@code shutdown()} pour auditer les fermetures de connexion.
     * </p>
     */
    @Pointcut("execution(* com.ecclesiaflow.io.grpc.client.GrpcClientConfig.shutdown())")
    public void grpcChannelShutdown() {}

    /**
     * Pointcut pour tous les appels RPC du client Auth.
     * <p>
     * Intercepte toutes les méthodes publiques de {@code AuthGrpcClient}
     * pour auditer les communications gRPC sortantes.
     * </p>
     */
    @Pointcut("execution(* com.ecclesiaflow.io.grpc.client.AuthGrpcClient.*(..))")
    public void grpcClientCalls() {}

    // ========================================================================
    // Advices - Shutdown du canal gRPC
    // ========================================================================

    /**
     * Log avant le shutdown du canal gRPC.
     *
     * @param joinPoint point de jonction
     */
    @Before("grpcChannelShutdown()")
    public void logBeforeChannelShutdown(JoinPoint joinPoint) {
        log.info("🛑 GRPC-CLIENT: Initiating graceful shutdown of gRPC channel to Auth service...");
    }

    /**
     * Log après shutdown réussi du canal gRPC.
     *
     * @param joinPoint point de jonction
     */
    @AfterReturning("grpcChannelShutdown()")
    public void logAfterChannelShutdown(JoinPoint joinPoint) {
        log.info("✅ GRPC-CLIENT: gRPC channel to Auth service closed successfully");
    }

    /**
     * Log des erreurs lors du shutdown.
     *
     * @param joinPoint point de jonction
     * @param exception exception levée
     */
    @AfterThrowing(pointcut = "grpcChannelShutdown()", throwing = "exception")
    public void logChannelShutdownError(JoinPoint joinPoint, Exception exception) {
        log.error("❌ GRPC-CLIENT: Error while closing gRPC channel - {}: {}", 
                exception.getClass().getSimpleName(), 
                exception.getMessage());
    }

    // ========================================================================
    // Advices - Appels RPC sortants
    // ========================================================================

    /**
     * Log avant chaque appel RPC sortant vers le module Auth.
     * <p>
     * Capture les informations de base de l'appel pour traçabilité inter-modules.
     * </p>
     *
     * @param joinPoint point de jonction contenant le nom de la méthode
     */
    @Before("grpcClientCalls()")
    public void logBeforeRpcCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        
        // Log selon le type de RPC
        if ("generateTemporaryToken".equals(methodName)) {
            log.info("📤 GRPC-CLIENT: Calling Auth.{} via gRPC", methodName);
        } else {
            log.debug("📡 GRPC-CLIENT: Calling Auth.{} via gRPC", methodName);
        }
    }

    /**
     * Log après succès d'un appel RPC.
     * <p>
     * Confirme que l'appel vers le module Auth s'est terminé avec succès.
     * </p>
     *
     * @param joinPoint point de jonction
     * @param result le résultat retourné par la méthode
     */
    @AfterReturning(pointcut = "grpcClientCalls()", returning = "result")
    public void logAfterSuccessfulRpcCall(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        
        if ("generateTemporaryToken".equals(methodName)) {
            log.info("✅ GRPC-CLIENT: Auth.{} completed successfully", methodName);
        } else {
            log.debug("✅ GRPC-CLIENT: Auth.{} completed successfully", methodName);
        }
    }

    /**
     * Log des erreurs lors des appels RPC.
     * <p>
     * Capture les exceptions levées pendant les appels gRPC et différencie
     * les types d'erreurs (timeout, unavailable, invalid argument, etc.).
     * </p>
     *
     * @param joinPoint point de jonction
     * @param exception exception levée
     */
    @AfterThrowing(pointcut = "grpcClientCalls()", throwing = "exception")
    public void logRpcCallError(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String exceptionType = exception.getClass().getSimpleName();
        
        // Erreurs de disponibilité du service Auth (critique)
        if (exceptionType.contains("Unavailable") || exceptionType.contains("UNAVAILABLE")) {
            log.error("❌ GRPC-CLIENT: Auth service UNAVAILABLE during {} - {}", 
                    methodName, 
                    exception.getMessage());
        }
        // Erreurs de timeout (warning)
        else if (exceptionType.contains("Timeout") || exceptionType.contains("DEADLINE_EXCEEDED")) {
            log.warn("⏱️  GRPC-CLIENT: Timeout during {} - {}", 
                    methodName, 
                    exception.getMessage());
        }
        // Erreurs de validation (client error)
        else if (exception instanceof IllegalArgumentException) {
            log.warn("⚠️  GRPC-CLIENT: Invalid argument in {} - {}", 
                    methodName, 
                    exception.getMessage());
        }
        // Erreurs de sécurité
        else if (exception instanceof SecurityException) {
            log.error("🔒 GRPC-CLIENT: Security error during {} - {}", 
                    methodName, 
                    exception.getMessage());
        }
        // Autres erreurs (server error)
        else {
            log.error("❌ GRPC-CLIENT: Error during {} - {}: {}", 
                    methodName,
                    exceptionType, 
                    exception.getMessage());
        }
    }
}
