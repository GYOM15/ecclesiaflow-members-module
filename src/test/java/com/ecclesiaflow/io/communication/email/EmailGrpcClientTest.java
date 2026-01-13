package com.ecclesiaflow.io.communication.email;

import com.ecclesiaflow.business.exceptions.EmailServiceException;
import com.ecclesiaflow.grpc.email.*;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EmailGrpcClient}.
 *
 * <p>Tests gRPC communication with the Email service including success
 * scenarios and error handling. Circuit breaker behavior is tested
 * separately in integration tests.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@DisplayName("EmailGrpcClient - Unit Tests")
class EmailGrpcClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private EmailGrpcClient emailGrpcClient;
    private TestEmailServiceImpl testService;
    private ManagedChannel channel;

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        testService = new TestEmailServiceImpl();

        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(testService)
                .build()
                .start());

        channel = grpcCleanup.register(InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build());

        emailGrpcClient = new EmailGrpcClient(channel);
    }

    @Nested
    @DisplayName("sendConfirmationEmail")
    class SendConfirmationEmail {

        @Test
        @DisplayName("should return email ID on successful send")
        void shouldReturnEmailIdOnSuccess() {
            // Given
            String email = "test@example.com";
            String confirmationUrl = "https://ecclesiaflow.com/confirm?token=abc123";
            UUID expectedId = UUID.randomUUID();
            testService.setResponseEmailId(expectedId.toString());

            // When
            UUID result = emailGrpcClient.sendConfirmationEmail(email, confirmationUrl);

            // Then
            assertThat(result).isEqualTo(expectedId);
            assertThat(testService.getLastRequest()).isNotNull();
            assertThat(testService.getLastRequest().getToList()).contains(email);
            assertThat(testService.getLastRequest().getTemplateType())
                    .isEqualTo(EmailTemplateType.EMAIL_TEMPLATE_EMAIL_CONFIRMATION);
        }

        @Test
        @DisplayName("should throw EmailServiceException on UNAVAILABLE status")
        void shouldThrowExceptionOnUnavailable() {
            // Given
            String email = "test@example.com";
            String confirmationUrl = "https://ecclesiaflow.com/confirm?token=abc123";
            testService.setErrorToThrow(Status.UNAVAILABLE.withDescription("Service down"));

            // When/Then
            assertThatThrownBy(() -> emailGrpcClient.sendConfirmationEmail(email, confirmationUrl))
                    .isInstanceOf(EmailServiceException.class)
                    .hasMessageContaining("UNAVAILABLE");
        }

        @Test
        @DisplayName("should throw EmailServiceException on DEADLINE_EXCEEDED status")
        void shouldThrowExceptionOnDeadlineExceeded() {
            // Given
            String email = "test@example.com";
            String confirmationUrl = "https://ecclesiaflow.com/confirm?token=abc123";
            testService.setErrorToThrow(Status.DEADLINE_EXCEEDED.withDescription("Timeout"));

            // When/Then
            assertThatThrownBy(() -> emailGrpcClient.sendConfirmationEmail(email, confirmationUrl))
                    .isInstanceOf(EmailServiceException.class)
                    .hasMessageContaining("DEADLINE_EXCEEDED");
        }

        @Test
        @DisplayName("should throw EmailServiceException on INTERNAL status")
        void shouldThrowExceptionOnInternalError() {
            // Given
            String email = "test@example.com";
            String confirmationUrl = "https://ecclesiaflow.com/confirm?token=abc123";
            testService.setErrorToThrow(Status.INTERNAL.withDescription("Internal error"));

            // When/Then
            assertThatThrownBy(() -> emailGrpcClient.sendConfirmationEmail(email, confirmationUrl))
                    .isInstanceOf(EmailServiceException.class)
                    .hasMessageContaining("INTERNAL");
        }

        @Test
        @DisplayName("should include email operation type in exception")
        void shouldIncludeOperationTypeInException() {
            // Given
            String email = "test@example.com";
            String confirmationUrl = "https://ecclesiaflow.com/confirm?token=abc123";
            testService.setErrorToThrow(Status.UNAVAILABLE);

            // When/Then
            assertThatThrownBy(() -> emailGrpcClient.sendConfirmationEmail(email, confirmationUrl))
                    .isInstanceOf(EmailServiceException.class)
                    .hasMessageContaining("CONFIRMATION");
        }
    }

    /**
     * Test implementation of the Email gRPC service.
     */
    private static class TestEmailServiceImpl extends EmailServiceGrpc.EmailServiceImplBase {

        private String responseEmailId = UUID.randomUUID().toString();
        private Status errorToThrow = null;
        private SendEmailRequest lastRequest = null;

        public void setResponseEmailId(String emailId) {
            this.responseEmailId = emailId;
        }

        public void setErrorToThrow(Status status) {
            this.errorToThrow = status;
        }

        public SendEmailRequest getLastRequest() {
            return lastRequest;
        }

        @Override
        public void sendEmail(SendEmailRequest request, StreamObserver<SendEmailResponse> responseObserver) {
            lastRequest = request;

            if (errorToThrow != null) {
                responseObserver.onError(new StatusRuntimeException(errorToThrow));
                return;
            }

            SendEmailResponse response = SendEmailResponse.newBuilder()
                    .setEmailId(responseEmailId)
                    .setStatus(com.ecclesiaflow.grpc.email.Status.STATUS_QUEUED)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
