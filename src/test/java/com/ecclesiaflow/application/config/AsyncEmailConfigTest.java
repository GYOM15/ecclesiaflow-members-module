package com.ecclesiaflow.application.config;

import org.junit.jupiter.api.Test;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour {@link AsyncEmailConfig}.
 * <p>
 * Vérifie la configuration correcte du thread pool dédié pour les emails
 * et du gestionnaire d'exceptions asynchrones.
 * </p>
 */
class AsyncEmailConfigTest {

    private final AsyncEmailConfig config = new AsyncEmailConfig();

    @Test
    void emailTaskExecutor_ShouldCreateExecutorWithCorrectConfiguration() {
        // when
        Executor executor = config.emailTaskExecutor();

        // then
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(2, taskExecutor.getCorePoolSize());
        assertEquals(5, taskExecutor.getMaxPoolSize());
        assertEquals(100, taskExecutor.getQueueCapacity());
        assertTrue(taskExecutor.getThreadNamePrefix().startsWith("email-async-"));
    }

    @Test
    void emailTaskExecutor_ShouldBeThreadPoolTaskExecutor() {
        // when
        Executor executor = config.emailTaskExecutor();

        // then
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
    }

    @Test
    void getAsyncUncaughtExceptionHandler_ShouldReturnNonNullHandler() {
        // when
        AsyncUncaughtExceptionHandler handler = config.getAsyncUncaughtExceptionHandler();

        // then
        assertNotNull(handler);
    }

    @Test
    void asyncExceptionHandler_ShouldThrowAsyncEmailException() throws NoSuchMethodException {
        // given
        AsyncUncaughtExceptionHandler handler = config.getAsyncUncaughtExceptionHandler();
        Throwable originalException = new RuntimeException("SMTP connection failed");
        Method method = this.getClass().getMethod("dummyMethod");
        Object[] params = new Object[]{"test@example.com"};

        // when/then
        assertThrows(AsyncEmailConfig.AsyncEmailException.class, () -> {
            assert handler != null;
            handler.handleUncaughtException(originalException, method, params);
        });
    }

    @Test
    void asyncEmailException_ShouldContainOriginalCause() {
        // given
        RuntimeException cause = new RuntimeException("Original error");

        // when
        AsyncEmailConfig.AsyncEmailException exception = 
            new AsyncEmailConfig.AsyncEmailException("Async email task failed: testMethod", cause);

        // then
        assertNotNull(exception);
        assertEquals("Async email task failed: testMethod", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void asyncEmailException_ShouldBeRuntimeException() {
        // given
        RuntimeException cause = new RuntimeException("Test");

        // when
        AsyncEmailConfig.AsyncEmailException exception = 
            new AsyncEmailConfig.AsyncEmailException("Test message", cause);

        // then
        assertTrue(exception instanceof RuntimeException);
    }

    // Méthode dummy pour les tests
    public void dummyMethod() {
        // Méthode de test
    }
}
