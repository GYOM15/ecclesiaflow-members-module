package com.ecclesiaflow.application.logging.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Aspect pour le logging du cycle de vie des canaux gRPC.
 * 
 * <p>Cet aspect gère tout le logging lié à l'initialisation et à la fermeture
 * des canaux gRPC, permettant à la couche de configuration de rester pure.</p>
 * 
 * <p><strong>Responsabilité :</strong> Logging centralisé du cycle de vie gRPC</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Aspect
@Component
public class GrpcChannelLifecycleLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(GrpcChannelLifecycleLoggingAspect.class);

    @Value("${grpc.email.host:localhost}")
    private String emailHost;

    @Value("${grpc.email.port:9092}")
    private int emailPort;

    @Pointcut("execution(* com.ecclesiaflow.application.config.GrpcEmailClientConfig.emailGrpcChannel())")
    public void grpcChannelCreation() {}

    @Pointcut("execution(* com.ecclesiaflow.application.config.GrpcEmailClientConfig.emailClient(..))")
    public void emailClientCreation() {}

    @Pointcut("execution(* com.ecclesiaflow.application.config.GrpcEmailClientConfig.shutdown())")
    public void grpcChannelShutdown() {}

    @Before("grpcChannelCreation()")
    public void logBeforeChannelCreation() {
        log.info("🔌 Initializing gRPC channel for Email service at {}:{}", emailHost, emailPort);
    }

    @AfterReturning("grpcChannelCreation()")
    public void logAfterChannelCreation() {
        log.info("- Email gRPC channel initialized successfully");
    }

    @AfterReturning("emailClientCreation()")
    public void logAfterClientCreation() {
        log.info("- EmailClient configured with gRPC implementation");
    }

    @Before("grpcChannelShutdown()")
    public void logBeforeShutdown() {
        log.info("- Shutting down Email gRPC channel...");
    }

    @AfterReturning("grpcChannelShutdown()")
    public void logAfterShutdown() {
        log.info("- Email gRPC channel shutdown successfully");
    }
}
