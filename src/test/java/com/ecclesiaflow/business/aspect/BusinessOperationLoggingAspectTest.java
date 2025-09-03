package com.ecclesiaflow.business.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ecclesiaflow.common.logging.aspect.BusinessOperationLoggingAspect;
import org.aspectj.lang.annotation.*;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration pour BusinessOperationLoggingAspect.
 * Valide le comportement de l'aspect pour l'audit des opérations métier critiques.
 */
@SpringBootTest(classes = BusinessOperationLoggingAspectTest.TestConfig.class)
@DisplayName("BusinessOperationLoggingAspect - Tests d'intégration")
class BusinessOperationLoggingAspectTest {

    @Autowired
    private MemberServiceImpl memberService;

    @Autowired
    private AuthModuleService authModuleService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    // === TESTS POUR L'ENREGISTREMENT DE MEMBRES ===
    @Test
    @DisplayName("Devrait logger le début et la réussite de l'enregistrement d'un membre")
    void shouldLogSuccessfulMemberRegistration() {
        // When
        memberService.registerMember("test@example.com");

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("Tentative d'enregistrement d'un nouveau membre");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.get(1).getFormattedMessage()).contains("Nouveau membre enregistré avec succès");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Devrait logger l'échec de l'enregistrement d'un membre")
    void shouldLogFailedMemberRegistration() {
        // When & Then
        assertThatThrownBy(() -> memberService.registerMember("throw_exception"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erreur d'enregistrement");

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("Tentative d'enregistrement d'un nouveau membre");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.get(1).getFormattedMessage()).contains("Échec de l'enregistrement du membre - Erreur d'enregistrement");
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
        assertThat(logs.get(0).getFormattedMessage()).contains("Appel à login avec arguments: [user, pass]");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getFormattedMessage()).contains("login a réussi avec résultat: token_123");
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
        assertThat(logs.get(0).getFormattedMessage()).contains("Appel à logout avec arguments: []");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getFormattedMessage()).contains("logout exécuté avec succès");
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
        assertThat(logs.get(0).getFormattedMessage()).contains("Appel à refreshToken avec arguments: [invalid_token]");
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getFormattedMessage()).contains("Échec de l'appel à refreshToken - Jeton invalide");
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.WARN);
    }

    // --- CLASSES DE TEST ET CONFIGURATION ---

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public BusinessOperationLoggingAspect businessOperationLoggingAspect() {
            return new BusinessOperationLoggingAspect();
        }

        // Services mock pour les tests d'intégration
        @Bean
        public MemberServiceImpl memberService() {
            return new MemberServiceImpl();
        }

        @Bean
        public AuthModuleService authModuleService() {
            return new AuthModuleService();
        }

        @Bean
        public TestBusinessOperationAspect testBusinessOperationAspect() {
            return new TestBusinessOperationAspect();
        }
    }

    @Service
    static class MemberServiceImpl {
        public void registerMember(String email) {
            if ("throw_exception".equals(email)) {
                throw new RuntimeException("Erreur d'enregistrement");
            }
        }
    }

    @Service
    static class AuthModuleService {
        public String login(String username, String password) {
            return "token_123";
        }

        public void logout() {}

        public String refreshToken(String token) {
            if ("invalid_token".equals(token)) {
                throw new RuntimeException("Jeton invalide");
            }
            return "new_token";
        }
    }

    /**
     * Aspect de test qui étend BusinessOperationLoggingAspect
     * pour lier ses pointcuts aux services de test locaux.
     */
    @Aspect
    @Component
    static class TestBusinessOperationAspect extends BusinessOperationLoggingAspect {

        @Pointcut("execution(* com.ecclesiaflow.business.aspect.BusinessOperationLoggingAspectTest.MemberServiceImpl.registerMember(..))")
        @Override
        public void memberRegistration() {}

        @Pointcut("execution(* com.ecclesiaflow.business.aspect.BusinessOperationLoggingAspectTest.AuthModuleService.*(..))")
        @Override
        public void authModuleCalls() {}
    }
}