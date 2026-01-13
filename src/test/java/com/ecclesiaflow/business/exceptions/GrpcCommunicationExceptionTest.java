package com.ecclesiaflow.business.exceptions;

import io.grpc.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GrpcCommunicationException")
class GrpcCommunicationExceptionTest {

    @Test
    @DisplayName("should build safe message without infrastructure details")
    void shouldBuildSafeMessage() {
        GrpcCommunicationException exception = new GrpcCommunicationException(
                "EmailService",
                "sendEmail",
                Status.Code.UNAVAILABLE,
                "Connection refused: 192.168.1.100:8080",
                new RuntimeException("root cause")
        );

        assertThat(exception.getMessage()).isEqualTo("gRPC call failed: EmailService.sendEmail - UNAVAILABLE");
        assertThat(exception.getMessage()).doesNotContain("192.168.1.100");
    }

    @Test
    @DisplayName("should sanitize description for logging")
    void shouldSanitizeDescription() {
        GrpcCommunicationException exception = new GrpcCommunicationException(
                "EmailService",
                "sendEmail",
                Status.Code.UNAVAILABLE,
                "Connection to 10.0.0.50:9090 refused",
                new RuntimeException("connection error")
        );

        String sanitized = exception.getSanitizedDescription();
        assertThat(sanitized).doesNotContain("10.0.0.50");
    }

    @Test
    @DisplayName("should expose service metadata")
    void shouldExposeServiceMetadata() {
        GrpcCommunicationException exception = new GrpcCommunicationException(
                "AuthService",
                "validateToken",
                Status.Code.DEADLINE_EXCEEDED,
                "Timeout",
                new RuntimeException("timeout")
        );

        assertThat(exception.getServiceName()).isEqualTo("AuthService");
        assertThat(exception.getMethodName()).isEqualTo("validateToken");
        assertThat(exception.getStatusCode()).isEqualTo(Status.Code.DEADLINE_EXCEEDED);
        assertThat(exception.getStatusDescription()).isEqualTo("Timeout");
    }
}
