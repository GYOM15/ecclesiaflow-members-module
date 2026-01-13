package com.ecclesiaflow.application.logging.aspect;

import com.ecclesiaflow.business.exceptions.EmailServiceException;
import com.ecclesiaflow.business.exceptions.EmailServiceUnavailableException;
import com.ecclesiaflow.business.exceptions.GrpcCommunicationException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.grpc.Status;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EmailGrpcClientLoggingAspect")
class EmailGrpcClientLoggingAspectTest {

    private EmailGrpcClientLoggingAspect aspect;
    private JoinPoint joinPoint;
    private Signature signature;

    @BeforeEach
    void setUp() {
        aspect = new EmailGrpcClientLoggingAspect();
        joinPoint = mock(JoinPoint.class);
        signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("sendConfirmationEmail");
    }

    @Nested
    @DisplayName("logBeforeEmailRpcCall")
    class LogBeforeTests {

        @Test
        @DisplayName("should log before RPC call without error")
        void shouldLogBeforeRpcCall() {
            assertThatNoException().isThrownBy(() -> aspect.logBeforeEmailRpcCall(joinPoint));
        }
    }

    @Nested
    @DisplayName("logAfterSuccessfulEmailRpcCall")
    class LogAfterSuccessTests {

        @Test
        @DisplayName("should log after successful call with UUID result")
        void shouldLogAfterSuccessWithUuid() {
            UUID result = UUID.randomUUID();
            assertThatNoException().isThrownBy(() -> 
                aspect.logAfterSuccessfulEmailRpcCall(joinPoint, result));
        }

        @Test
        @DisplayName("should log after successful call with null result")
        void shouldLogAfterSuccessWithNull() {
            assertThatNoException().isThrownBy(() -> 
                aspect.logAfterSuccessfulEmailRpcCall(joinPoint, null));
        }
    }

    @Nested
    @DisplayName("logEmailRpcCallError")
    class LogErrorTests {

        @Test
        @DisplayName("should log CallNotPermittedException")
        void shouldLogCallNotPermitted() {
            CallNotPermittedException exception = mock(CallNotPermittedException.class);
            assertThatNoException().isThrownBy(() -> 
                aspect.logEmailRpcCallError(joinPoint, exception));
        }

        @Test
        @DisplayName("should log EmailServiceUnavailableException")
        void shouldLogServiceUnavailable() {
            EmailServiceUnavailableException exception = 
                new EmailServiceUnavailableException("EmailService", new RuntimeException("timeout"));
            assertThatNoException().isThrownBy(() -> 
                aspect.logEmailRpcCallError(joinPoint, exception));
        }

        @Test
        @DisplayName("should log EmailServiceException")
        void shouldLogEmailServiceException() {
            EmailServiceException exception = new EmailServiceException(
                "Failed", "test@example.com", EmailServiceException.EmailOperation.CONFIRMATION);
            assertThatNoException().isThrownBy(() -> 
                aspect.logEmailRpcCallError(joinPoint, exception));
        }

        @Test
        @DisplayName("should log GrpcCommunicationException")
        void shouldLogGrpcCommunicationException() {
            GrpcCommunicationException exception = new GrpcCommunicationException(
                "EmailService", "sendEmail", Status.Code.UNAVAILABLE, "Connection refused",
                new RuntimeException("cause"));
            assertThatNoException().isThrownBy(() -> 
                aspect.logEmailRpcCallError(joinPoint, exception));
        }

        @Test
        @DisplayName("should log generic RuntimeException")
        void shouldLogGenericException() {
            RuntimeException exception = new RuntimeException("Unknown error");
            assertThatNoException().isThrownBy(() -> 
                aspect.logEmailRpcCallError(joinPoint, exception));
        }
    }
}
