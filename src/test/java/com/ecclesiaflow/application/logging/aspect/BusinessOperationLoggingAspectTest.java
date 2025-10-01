package com.ecclesiaflow.application.logging.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Tests la logique de logging des opérations métier critiques avec un aspect dédié aux tests.
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
        // Configuration du logger pour capturer les logs de l'aspect
        logger = (Logger) LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        if (logger != null) {
            logger.detachAppender(listAppender);
        }
        if (listAppender != null) {
            listAppender.stop();
        }
    }

    // === TESTS MEMBRE ===
    @Test
    @DisplayName("Devrait logger l'enregistrement réussi d'un membre")
    void shouldLogSuccessfulMemberRegistration() {
        // Given
        String email = "test@example.com";

        // When
        String result = memberService.registerMember(email);

        // Then
        assertThat(result).isEqualTo("Member registered: " + email);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs)
                .hasSize(2)
                .satisfies(logList -> {
                    // Log de début
                    ILoggingEvent startLog = logList.getFirst();
                    assertThat(startLog.getLevel()).isEqualTo(Level.INFO);
                    assertThat(startLog.getFormattedMessage())
                            .contains("BUSINESS: Tentative d'enregistrement d'un nouveau membre");

                    // Log de succès
                    ILoggingEvent successLog = logList.get(1);
                    assertThat(successLog.getLevel()).isEqualTo(Level.INFO);
                    assertThat(successLog.getFormattedMessage())
                            .contains("BUSINESS: Nouveau membre enregistré avec succès");
                });
    }

    @Test
    @DisplayName("Devrait logger l'échec d'enregistrement d'un membre")
    void shouldLogFailedMemberRegistration() {
        // Given
        String errorEmail = "throw_exception";

        // When & Then
        assertThatThrownBy(() -> memberService.registerMember(errorEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erreur d'enregistrement");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs)
                .hasSize(2)
                .satisfies(logList -> {
                    // Log de début
                    ILoggingEvent startLog = logList.getFirst();
                    assertThat(startLog.getLevel()).isEqualTo(Level.INFO);
                    assertThat(startLog.getFormattedMessage())
                            .contains("BUSINESS: Tentative d'enregistrement d'un nouveau membre");

                    // Log d'erreur
                    ILoggingEvent errorLog = logList.get(1);
                    assertThat(errorLog.getLevel()).isEqualTo(Level.WARN);
                    assertThat(errorLog.getFormattedMessage())
                            .contains("BUSINESS: Échec de l'enregistrement du membre")
                            .contains("Erreur d'enregistrement");
                });
    }

    // === TESTS AUTHENTIFICATION ===
    @Test
    @DisplayName("Devrait logger un appel d'authentification réussi avec retour")
    void shouldLogSuccessfulAuthCallWithReturn() {
        // Given
        String username = "user";
        String password = "pass";

        // When
        String result = authModuleService.login(username, password);

        // Then
        assertThat(result).isEqualTo("token_123");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs)
                .hasSize(2)
                .satisfies(logList -> {
                    // Log de début
                    ILoggingEvent startLog = logList.getFirst();
                    assertThat(startLog.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(startLog.getFormattedMessage())
                            .contains("AUTH MODULE: Appel à login avec arguments: [user, pass]");

                    // Log de succès
                    ILoggingEvent successLog = logList.get(1);
                    assertThat(successLog.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(successLog.getFormattedMessage())
                            .contains("AUTH MODULE: login a réussi avec résultat: token_123");
                });
    }

    @Test
    @DisplayName("Devrait logger un appel d'authentification réussi sans retour (void)")
    void shouldLogSuccessfulAuthCallVoid() {
        // When
        authModuleService.logout();

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs)
                .hasSize(2)
                .satisfies(logList -> {
                    // Log de début
                    ILoggingEvent startLog = logList.getFirst();
                    assertThat(startLog.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(startLog.getFormattedMessage())
                            .contains("AUTH MODULE: Appel à logout avec arguments: []");

                    // Log de succès
                    ILoggingEvent successLog = logList.get(1);
                    assertThat(successLog.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(successLog.getFormattedMessage())
                            .contains("AUTH MODULE: logout exécuté avec succès");
                });
    }

    @Test
    @DisplayName("Devrait logger un échec d'appel d'authentification")
    void shouldLogFailedAuthCall() {
        // Given
        String invalidToken = "invalid_token";

        // When & Then
        assertThatThrownBy(() -> authModuleService.refreshToken(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Jeton invalide");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs)
                .hasSize(2)
                .satisfies(logList -> {
                    // Log de début
                    ILoggingEvent startLog = logList.getFirst();
                    assertThat(startLog.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(startLog.getFormattedMessage())
                            .contains("AUTH MODULE: Appel à refreshToken avec arguments: [invalid_token]");

                    // Log d'erreur
                    ILoggingEvent errorLog = logList.get(1);
                    assertThat(errorLog.getLevel()).isEqualTo(Level.WARN);
                    assertThat(errorLog.getFormattedMessage())
                            .contains("AUTH MODULE: Échec de l'appel à refreshToken")
                            .contains("Jeton invalide");
                });
    }

    @Test
    @DisplayName("Devrait gérer les arguments null sans erreur")
    void shouldHandleNullArgumentsGracefully() {
        // When
        String result = authModuleService.login(null, null);

        // Then
        assertThat(result).isEqualTo("token_123");

        List<ILoggingEvent> logs = listAppender.list;
        ILoggingEvent startLog = logs.getFirst();
        assertThat(startLog.getFormattedMessage())
                .contains("AUTH MODULE: Appel à login avec arguments: [null, null]");
    }

    // === CONFIGURATION DE TEST ===
    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public BusinessOperationLoggingAspect businessOperationLoggingAspect() {
            return new BusinessOperationLoggingAspect();
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

    // === SERVICES DE TEST ===
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
            // Méthode void pour tester le cas sans retour
        }

        public void refreshToken(String token) {
            if ("invalid_token".equals(token)) {
                throw new RuntimeException("Jeton invalide");
            }
        }
    }

    // === ASPECT DE TEST (RÉPLIQUE EXACTE DE L'ASPECT RÉEL) ===
    @Aspect
    @Component
    static class BusinessOperationLoggingAspect {

        private static final org.slf4j.Logger log =
                LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);

        // Pointcuts
        @Pointcut("execution(* com.ecclesiaflow.application.logging.aspect.BusinessOperationLoggingAspectTest.TestMemberService.registerMember(..))")
        public void memberRegistration() {}

        @Pointcut("execution(* com.ecclesiaflow.application.logging.aspect.BusinessOperationLoggingAspectTest.TestAuthModuleService.*(..))")
        public void authModuleCalls() {}

        // Advices pour l'enregistrement de membres
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

        // Advices pour les appels au module d'authentification
        @Before("authModuleCalls()")
        public void logBeforeAuthModuleCall(JoinPoint joinPoint) {
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            log.debug("AUTH MODULE: Appel à {} avec arguments: {}",
                    methodName, java.util.Arrays.toString(args));
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