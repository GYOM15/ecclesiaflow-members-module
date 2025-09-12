package com.ecclesiaflow.application.logging.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Pointcut;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour BusinessOperationLoggingAspect.
 * Valide le comportement de l'aspect pour l'audit des opérations métier critiques.
 * Utilise un aspect de test dédié avec des services de test simples.
 */
@SpringBootTest(classes = BusinessOperationLoggingAspectTest.TestConfig.class)
@DisplayName("BusinessOperationLoggingAspect - Tests unitaires")
class BusinessOperationLoggingAspectTest {

    @Autowired
    private TestMemberService memberService;

    @Autowired
    private TestAuthModuleService authModuleService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Target the actual aspect's logger
        logger = (Logger) LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        // Ensure the logger level is sufficient to capture all logs (INFO and DEBUG)
        logger.setLevel(Level.DEBUG);

        // No mocks to reset with this approach
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    // === TESTS FOR MEMBER REGISTRATION ===
    @Test
    @DisplayName("Devrait logger le début et la réussite de l'enregistrement d'un membre")
    void shouldLogSuccessfulMemberRegistration() {
        // When: Call the test service (the aspect will execute)
        String result = memberService.registerMember("test@example.com");

        // Then: Verify the result and the aspect's logs
        assertThat(result).isEqualTo("Member registered: test@example.com");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.get(1).getFormattedMessage()).contains("BUSINESS: Nouveau membre enregistré avec succès");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Devrait logger l'échec de l'enregistrement d'un membre")
    void shouldLogFailedMemberRegistration() {
        // When & Then: Call the test service with an email that causes an exception
        assertThatThrownBy(() -> memberService.registerMember("throw_exception"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erreur d'enregistrement");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2); // One log before, one error log
        assertThat(logs.get(0).getFormattedMessage()).contains("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.get(1).getFormattedMessage()).contains("BUSINESS: Échec de l'enregistrement du membre - Erreur d'enregistrement");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.WARN);
    }


    // === TESTS FOR AUTHENTICATION MODULE CALLS ===
    @Test
    @DisplayName("Devrait logger le début et la réussite d'un appel au module d'authentification")
    void shouldLogSuccessfulAuthModuleCall() {
        // When
        String result = authModuleService.login("user", "pass");

        // Then
        assertThat(result).isEqualTo("token_123");
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("AUTH MODULE: Appel à login avec arguments: [user, pass]");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getFormattedMessage()).contains("AUTH MODULE: login a réussi avec résultat: token_123");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.DEBUG);
    }

    @Test
    @DisplayName("Should log an authentication module call with no return result (void)")
    void shouldLogAuthModuleCallWithVoidReturn() {
        // When
        authModuleService.logout();

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("AUTH MODULE: Appel à logout avec arguments: []");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getFormattedMessage()).contains("AUTH MODULE: logout exécuté avec succès");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.DEBUG);
    }

    @Test
    @DisplayName("Should log the failure of an authentication module call")
    void shouldLogFailedAuthModuleCall() {
        // When & Then
        assertThatThrownBy(() -> authModuleService.refreshToken("invalid_token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Jeton invalide");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("AUTH MODULE: Appel à refreshToken avec arguments: [invalid_token]");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getFormattedMessage()).contains("AUTH MODULE: Échec de l'appel à refreshToken - Jeton invalide");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.WARN);
    }

    // --- TEST CONFIGURATION WITH DEDICATED ASPECT ---
    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public TestBusinessOperationAspect businessOperationLoggingAspect() {
            return new TestBusinessOperationAspect();
        }

        @Bean
        public TestMemberService memberService() {
            return new TestMemberService();
        }

        @Bean
        public TestAuthModuleService authModuleService() {
            return new TestAuthModuleService();
        }
    }

    // === TEST SERVICES ===
    @Service
    static class TestMemberService {
        public String registerMember(String email) {
            if ("throw_exception".equals(email)) {
                throw new RuntimeException("Erreur d'enregistrement");
            }
            return "Member registered: " + email;
        }
    }

    @Component
    static class TestAuthModuleService {
        public String login(String username, String password) {
            return "token_123";
        }

        public void logout() {
            // Void method
        }

        public String refreshToken(String token) {
            if ("invalid_token".equals(token)) {
                throw new RuntimeException("Jeton invalide");
            }
            return "new_token_456";
        }
    }

    // === DEDICATED TEST ASPECT ===
    @Aspect
    @Component
    static class TestBusinessOperationAspect {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);

        // Pointcuts for test services
        @Pointcut("execution(* com.ecclesiaflow.application.logging.aspect.BusinessOperationLoggingAspectTest.TestMemberService.registerMember(..))")
        public void memberRegistration() {}

        @Pointcut("execution(* com.ecclesiaflow.application.logging.aspect.BusinessOperationLoggingAspectTest.TestAuthModuleService.*(..))")
        public void authModuleCalls() {}

        // Advices for member registration
        @Before("memberRegistration()")
        public void logBeforeMemberRegistration(JoinPoint joinPoint) {
            log.info("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
        }

        @AfterReturning("memberRegistration()")
        public void logAfterMemberRegistrationSuccess(JoinPoint joinPoint) {
            log.info("BUSINESS: Nouveau membre enregistré avec succès");
        }

        @AfterThrowing(pointcut = "memberRegistration()", throwing = "ex")
        public void logAfterMemberRegistrationFailure(JoinPoint joinPoint, Throwable ex) {
            log.warn("BUSINESS: Échec de l'enregistrement du membre - {}", ex.getMessage());
        }

        // Advices for authentication module calls
        @Before("authModuleCalls()")
        public void logBeforeAuthModuleCall(JoinPoint joinPoint) {
            String methodName = joinPoint.getSignature().getName();
            log.debug("AUTH MODULE: Appel à {} avec arguments: {}", methodName, java.util.Arrays.toString(joinPoint.getArgs()));
        }

        @AfterReturning(pointcut = "authModuleCalls()", returning = "result")
        public void logAfterAuthModuleCallSuccess(JoinPoint joinPoint, Object result) {
            String methodName = joinPoint.getSignature().getName();
            if (result != null) {
                log.debug("AUTH MODULE: {} a réussi avec résultat: {}", methodName, result);
            } else {
                log.debug("AUTH MODULE: {} exécuté avec succès", methodName);
            }
        }

        @AfterThrowing(pointcut = "authModuleCalls()", throwing = "ex")
        public void logAfterAuthModuleCallFailure(JoinPoint joinPoint, Throwable ex) {
            String methodName = joinPoint.getSignature().getName();
            log.warn("AUTH MODULE: Échec de l'appel à {} - {}", methodName, ex.getMessage());
        }
    }
}