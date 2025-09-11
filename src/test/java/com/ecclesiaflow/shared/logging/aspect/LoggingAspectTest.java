package com.ecclesiaflow.shared.logging.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ecclesiaflow.shared.logging.annotation.LogExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration pour LoggingAspect.
 * * Ces tests utilisent un contexte Spring complet pour permettre le weaving AOP.
 * Contrairement aux tests unitaires avec mocks, ces tests permettent aux aspects
 * de fonctionner correctement en créant de vrais proxies Spring AOP.
 */
@SpringBootTest(classes = {LoggingAspectTest.TestConfig.class})
@TestPropertySource(properties = {
        "logging.level.com.ecclesiaflow.shared.logging.aspect.LoggingAspect=DEBUG"
})
@DisplayName("LoggingAspect - Tests d'intégration AOP")
class LoggingAspectTest {

    // Les services sont maintenant des classes internes statiques
    @Autowired
    private TestService testService;

    @Autowired
    private TestAnnotatedService testAnnotatedService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Configuration du logger pour capturer les logs dans les tests
        logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        // Nettoyage après chaque test
        logger.detachAppender(listAppender);
        listAppender.stop(); // Arrêter l'appender après utilisation
    }

    @Test
    @DisplayName("Devrait logger les méthodes de service avec succès")
    void shouldLogServiceMethodsSuccessfully() {
        // When
        String result = testService.testMethod();

        // Then
        assertThat(result).isEqualTo("test result");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
            assertThat(event.getFormattedMessage()).contains("SERVICE: Début TestService.testMethod");
        });
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
            assertThat(event.getFormattedMessage()).contains("SERVICE: TestService.testMethod - Succès");
        });
    }

    @Test
    @DisplayName("Devrait logger les exceptions dans les méthodes de service")
    void shouldLogServiceMethodExceptions() {
        // When & Then
        assertThatThrownBy(() -> testService.exceptionMethod())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("SERVICE: TestService.exceptionMethod - Échec");
            assertThat(event.getFormattedMessage()).contains("Test exception");
        });
    }

    @Test
    @DisplayName("Devrait détecter les méthodes lentes (> 1 seconde)")
    void shouldDetectSlowMethods() {
        // When
        String result = testService.slowMethod();

        // Then
        assertThat(result).isEqualTo("slow result");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
            assertThat(event.getFormattedMessage()).contains("SERVICE: Début TestService.slowMethod");
        });
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).contains("SERVICE: TestService.slowMethod - Exécution lente");
        });
    }

    @Test
    @DisplayName("Devrait logger les méthodes annotées avec @LogExecution (cas par défaut)")
    void shouldLogAnnotatedMethods() {
        // When
        String result = testAnnotatedService.annotatedMethod();

        // Then
        assertThat(result).isEqualTo("annotated result");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Début: TestAnnotatedService.annotatedMethod");
        });
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Succès: TestAnnotatedService.annotatedMethod");
        });
    }

    @Test
    @DisplayName("Devrait utiliser un message personnalisé dans @LogExecution")
    void shouldUseCustomMessageInLogExecution() {
        // When
        String result = testAnnotatedService.customMessageMethod();

        // Then
        assertThat(result).isEqualTo("custom result");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Début: Custom operation");
        });
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Succès: Custom operation");
        });
    }

    @Test
    @DisplayName("Devrait inclure les paramètres quand demandé dans @LogExecution")
    void shouldIncludeParametersWhenRequested() {
        // When
        String result = testAnnotatedService.paramsMethod("param1", "param2");

        // Then
        assertThat(result).isEqualTo("params result");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Paramètres: [param1, param2]");
        });
    }

    @Test
    @DisplayName("Devrait logger les exceptions dans les méthodes annotées")
    void shouldLogAnnotatedMethodExceptions() {
        // When & Then
        assertThatThrownBy(() -> testAnnotatedService.exceptionAnnotatedMethod())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Annotation test exception");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("Échec: TestAnnotatedService.exceptionAnnotatedMethod");
            assertThat(event.getFormattedMessage()).contains("Annotation test exception");
        });
    }

    @Test
    @DisplayName("Devrait inclure le temps d'exécution quand demandé")
    void shouldIncludeExecutionTime() {
        // When
        String result = testAnnotatedService.executionTimeMethod();

        // Then
        assertThat(result).isEqualTo("time result");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Début: TestAnnotatedService.executionTimeMethod");
        });
        assertThat(logsList).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("Succès: TestAnnotatedService.executionTimeMethod");
            assertThat(event.getFormattedMessage()).contains("ms)");
        });
    }

    // Configuration de test Spring pour activer AOP
    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public TestLoggingAspect testLoggingAspect() {
            return new TestLoggingAspect();
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }

        @Bean
        public TestAnnotatedService testAnnotatedService() {
            return new TestAnnotatedService();
        }
    }

    // CHANGEMENT: La classe est rendue STATIQUE
    @Service
    static class TestService {

        public String testMethod() {
            return "test result";
        }

        public String slowMethod() {
            try {
                Thread.sleep(1100); // Simuler une exécution lente > 1 seconde
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "slow result";
        }

        public String exceptionMethod() {
            throw new RuntimeException("Test exception");
        }
    }

    // CHANGEMENT: La classe est rendue STATIQUE
    @Service
    static class TestAnnotatedService {

        @LogExecution // Assurez-vous que l'annotation est bien importée
        public String annotatedMethod() {
            return "annotated result";
        }

        @LogExecution(value = "Custom operation")
        public String customMessageMethod() {
            return "custom result";
        }

        @LogExecution(includeParams = true)
        public String paramsMethod(String param1, String param2) {
            return "params result";
        }

        @LogExecution(includeExecutionTime = true)
        public String executionTimeMethod() {
            return "time result";
        }

        @LogExecution
        public String exceptionAnnotatedMethod() {
            throw new RuntimeException("Annotation test exception");
        }
    }

    // CHANGEMENT: La classe est rendue STATIQUE
    @Aspect
    @Component
    static class TestLoggingAspect extends LoggingAspect {

        // CHANGEMENT: Pointcut cible les noms de classes internes statiques avec la syntaxe AspectJ correcte
        @Pointcut("execution(* com.ecclesiaflow.shared.logging.aspect.LoggingAspectTest.TestService.*(..))")
        public void testServiceMethods() {}

        // Advice pour les méthodes de service de test
        @Around("testServiceMethods()")
        public Object logTestServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
            return super.logServiceMethods(joinPoint); // Utilisation de la méthode publique
        }
    }
}