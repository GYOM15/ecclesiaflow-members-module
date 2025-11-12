package com.ecclesiaflow.application.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour AsyncConfig.
 * Vérifie la configuration du pool de threads asynchrone et la gestion des erreurs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AsyncConfig - Tests Unitaires")
class AsyncConfigTest {

    private AsyncConfig asyncConfig;

    @BeforeEach
    void setUp() {
        asyncConfig = new AsyncConfig();
    }

    @Test
    @DisplayName("Devrait configurer l'exécuteur avec les bonnes propriétés")
    void getAsyncExecutor_ShouldConfigureExecutorWithCorrectProperties() {
        // When
        Executor executor = asyncConfig.getAsyncExecutor();

        // Then
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assert threadPoolExecutor != null;
        assertThat(threadPoolExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(threadPoolExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(threadPoolExecutor.getQueueCapacity()).isEqualTo(100);
        assertThat(threadPoolExecutor.getKeepAliveSeconds()).isEqualTo(60);
        assertThat(threadPoolExecutor.getThreadNamePrefix()).isEqualTo("EcclesiaFlow-Async-");
    }

    @Test
    @DisplayName("Devrait configurer la politique de rejet CallerRunsPolicy")
    void getAsyncExecutor_ShouldConfigureCallerRunsPolicy() {
        // When
        Executor executor = asyncConfig.getAsyncExecutor();

        // Then
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assert threadPoolExecutor != null;
        RejectedExecutionHandler rejectedHandler = threadPoolExecutor.getThreadPoolExecutor().getRejectedExecutionHandler();
        assertThat(rejectedHandler).isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
    }

    @Test
    @DisplayName("Devrait configurer l'attente de fin des tâches lors de l'arrêt")
    void getAsyncExecutor_ShouldConfigureShutdownBehavior() {
        // When
        Executor executor = asyncConfig.getAsyncExecutor();

        // Then
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        // Note: Les propriétés de shutdown sont configurées mais ne sont pas exposées via des getters publics
        // On vérifie que l'exécuteur est correctement initialisé et fonctionnel
        assert threadPoolExecutor != null;
        assertThat(threadPoolExecutor.getThreadPoolExecutor()).isNotNull();
        assertThat(threadPoolExecutor.getThreadPoolExecutor().isShutdown()).isFalse();
    }

    @Test
    @DisplayName("Devrait retourner un gestionnaire d'exceptions asynchrones")
    void getAsyncUncaughtExceptionHandler_ShouldReturnExceptionHandler() {
        // When
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();

        // Then
        assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("Le gestionnaire d'exceptions devrait gérer les exceptions sans lever d'erreur")
    void asyncUncaughtExceptionHandler_ShouldHandleExceptionsGracefully() throws Exception {
        // Given
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method testMethod = AsyncConfig.class.getMethod("getAsyncExecutor");
        RuntimeException testException = new RuntimeException("Test exception");
        Object[] params = {"param1", "param2"};

        // When & Then - Ne devrait pas lever d'exception
        assertThatCode(() -> {
            assert handler != null;
            handler.handleUncaughtException(testException, testMethod, params);
        })
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Devrait créer un exécuteur initialisé et prêt à l'emploi")
    void getAsyncExecutor_ShouldReturnInitializedExecutor() {
        // When
        Executor executor = asyncConfig.getAsyncExecutor();

        // Then
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        
        // Vérifier que l'exécuteur est initialisé
        assert threadPoolExecutor != null;
        assertThat(threadPoolExecutor.getThreadPoolExecutor()).isNotNull();
        assertThat(threadPoolExecutor.getThreadPoolExecutor().isShutdown()).isFalse();
        assertThat(threadPoolExecutor.getThreadPoolExecutor().isTerminated()).isFalse();
    }

    @Test
    @DisplayName("Devrait configurer des paramètres de pool optimaux pour les opérations I/O")
    void getAsyncExecutor_ShouldHaveOptimalSettingsForIOOperations() {
        // When
        Executor executor = asyncConfig.getAsyncExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        // Then - Vérifier que la configuration est adaptée aux opérations I/O (email)
        // Core pool size relativement petit pour économiser les ressources
        assert threadPoolExecutor != null;
        assertThat(threadPoolExecutor.getCorePoolSize()).isLessThanOrEqualTo(5);
        
        // Max pool size suffisant pour gérer les pics de charge
        assertThat(threadPoolExecutor.getMaxPoolSize()).isGreaterThanOrEqualTo(5);
        
        // Queue capacity importante pour absorber les pics
        assertThat(threadPoolExecutor.getQueueCapacity()).isGreaterThanOrEqualTo(50);
        
        // Keep alive time raisonnable pour libérer les threads inactifs
        assertThat(threadPoolExecutor.getKeepAliveSeconds()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Devrait avoir un nom de thread descriptif pour le debugging")
    void getAsyncExecutor_ShouldHaveDescriptiveThreadName() {
        // When
        Executor executor = asyncConfig.getAsyncExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        // Then
        assert threadPoolExecutor != null;
        String threadNamePrefix = threadPoolExecutor.getThreadNamePrefix();
        assertThat(threadNamePrefix)
                .isNotNull()
                .isNotEmpty()
                .contains("EcclesiaFlow")
                .contains("Async");
    }
}
