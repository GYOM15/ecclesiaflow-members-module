package com.ecclesiaflow.business.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailServiceException")
class EmailServiceExceptionTest {

    @Test
    @DisplayName("should mask email address in getMaskedEmailAddress")
    void shouldMaskEmailAddress() {
        EmailServiceException exception = new EmailServiceException(
                "Failed to send",
                "john.doe@example.com",
                EmailServiceException.EmailOperation.CONFIRMATION
        );

        assertThat(exception.getMaskedEmailAddress()).isEqualTo("jo****@example.com");
        assertThat(exception.getOperation()).isEqualTo(EmailServiceException.EmailOperation.CONFIRMATION);
        assertThat(exception.getMessage()).isEqualTo("Failed to send");
    }

    @Test
    @DisplayName("should include cause when provided")
    void shouldIncludeCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        EmailServiceException exception = new EmailServiceException(
                "Failed to send",
                "test@mail.com",
                EmailServiceException.EmailOperation.WELCOME,
                cause
        );

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOperation()).isEqualTo(EmailServiceException.EmailOperation.WELCOME);
    }

    @Test
    @DisplayName("should handle all email operations")
    void shouldHandleAllOperations() {
        for (EmailServiceException.EmailOperation op : EmailServiceException.EmailOperation.values()) {
            EmailServiceException exception = new EmailServiceException("msg", "a@b.com", op);
            assertThat(exception.getOperation()).isEqualTo(op);
        }
    }
}
