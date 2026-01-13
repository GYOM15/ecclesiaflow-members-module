package com.ecclesiaflow.application.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResilienceConfig")
class ResilienceConfigTest {

    private final ResilienceConfig config = new ResilienceConfig();

    @Test
    @DisplayName("should create circuit breaker registry with email service config")
    void shouldCreateCircuitBreakerRegistry() {
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();

        assertThat(registry).isNotNull();
        assertThat(registry.circuitBreaker(ResilienceConfig.EMAIL_SERVICE_CB)).isNotNull();
    }

    @Test
    @DisplayName("should create retry registry with email service config")
    void shouldCreateRetryRegistry() {
        RetryRegistry registry = config.retryRegistry();

        assertThat(registry).isNotNull();
        assertThat(registry.retry(ResilienceConfig.EMAIL_SERVICE_RETRY)).isNotNull();
    }

    @Test
    @DisplayName("should have correct constant names")
    void shouldHaveCorrectConstantNames() {
        assertThat(ResilienceConfig.EMAIL_SERVICE_CB).isEqualTo("emailService");
        assertThat(ResilienceConfig.EMAIL_SERVICE_RETRY).isEqualTo("emailServiceRetry");
    }
}
