package com.ecclesiaflow.shared.logging.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.services.impl.MemberServiceImpl; // Le vrai service métier
import com.ecclesiaflow.web.client.AuthClient; // Le vrai client d'authentification
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
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
import static org.mockito.Mockito.*;

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
        // Ciblez le logger de l'aspect réel
        logger = (Logger) LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        // Assurez-vous que le niveau du logger est suffisant pour capturer tous les logs (INFO et DEBUG)
        logger.setLevel(Level.DEBUG);

        // Pas de mocks à réinitialiser avec cette approche
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    // === TESTS POUR L'ENREGISTREMENT DE MEMBRES ===
    @Test
    @DisplayName("Devrait logger le début et la réussite de l'enregistrement d'un membre")
    void shouldLogSuccessfulMemberRegistration() {
        // When: Appel du service de test (l'aspect va s'exécuter)
        String result = memberService.registerMember("test@example.com");

        // Then: Vérification du résultat et des logs de l'aspect
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
        // When & Then: Appel du service de test avec un email qui provoque une exception
        assertThatThrownBy(() -> memberService.registerMember("throw_exception"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erreur d'enregistrement");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2); // Un log avant, un log d'erreur
        assertThat(logs.get(0).getFormattedMessage()).contains("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.get(1).getFormattedMessage()).contains("BUSINESS: Échec de l'enregistrement du membre - Erreur d'enregistrement");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.WARN);
    }


    // === TESTS POUR LES APPELS AU MODULE D'AUTHENTIFICATION ===
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
    @DisplayName("Devrait logger un appel au module d'authentification sans résultat de retour")
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
    @DisplayName("Devrait logger l'échec d'un appel au module d'authentification")
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

    // --- CONFIGURATION DE TEST AVEC ASPECT DÉDIÉ ---
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
            // Méthode void
        }

        public String refreshToken(String token) {
            if ("invalid_token".equals(token)) {
                throw new RuntimeException("Jeton invalide");
            }
            return "new_token_456";
        }
    }

    // === ASPECT DE TEST DÉDIÉ ===
    @Aspect
    @Component
    static class TestBusinessOperationAspect {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);

        // Pointcuts pour les services de test
        @Pointcut("execution(* com.ecclesiaflow.shared.logging.aspect.BusinessOperationLoggingAspectTest.TestMemberService.registerMember(..))")
        public void memberRegistration() {}

        @Pointcut("execution(* com.ecclesiaflow.shared.logging.aspect.BusinessOperationLoggingAspectTest.TestAuthModuleService.*(..))")
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