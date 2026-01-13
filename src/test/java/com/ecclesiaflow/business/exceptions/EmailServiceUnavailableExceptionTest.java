package com.ecclesiaflow.business.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailServiceUnavailableException")
class EmailServiceUnavailableExceptionTest {

    @Test
    @DisplayName("should include service name in message")
    void shouldIncludeServiceNameInMessage() {
        RuntimeException cause = new RuntimeException("Connection refused");
        EmailServiceUnavailableException exception = new EmailServiceUnavailableException("EmailService", cause);

        assertThat(exception.getMessage()).contains("EmailService");
        assertThat(exception.getMessage()).contains("temporarily unavailable");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("should expose service name")
    void shouldExposeServiceName() {
        EmailServiceUnavailableException exception = new EmailServiceUnavailableException(
                "NotificationService", 
                new RuntimeException("timeout")
        );

        assertThat(exception.getServiceName()).isEqualTo("NotificationService");
    }
}
