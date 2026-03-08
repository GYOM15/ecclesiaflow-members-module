package com.ecclesiaflow.io.communication.email;

import com.ecclesiaflow.application.config.ResilienceConfig;
import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.exceptions.EmailServiceException;
import com.ecclesiaflow.business.exceptions.EmailServiceException.EmailOperation;
import com.ecclesiaflow.business.exceptions.EmailServiceUnavailableException;
import com.ecclesiaflow.business.exceptions.GrpcCommunicationException;
import com.ecclesiaflow.grpc.email.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * gRPC adapter implementing {@link EmailClient} port with circuit breaker.
 */
@Component
public class EmailGrpcClient implements EmailClient {

    private static final String SERVICE_NAME = "EmailService";

    private final EmailServiceGrpc.EmailServiceBlockingStub stub;

    public EmailGrpcClient(ManagedChannel emailGrpcChannel) {
        this.stub = EmailServiceGrpc.newBlockingStub(emailGrpcChannel)
                .withCompression("gzip");
    }

    @Override
    @CircuitBreaker(name = ResilienceConfig.EMAIL_SERVICE_CB, fallbackMethod = "sendConfirmationEmailFallback")
    @Retry(name = ResilienceConfig.EMAIL_SERVICE_RETRY)
    public UUID sendConfirmationEmail(String email, String confirmationUrl, String firstName) {
        Map<String, String> variables = Map.of(
            "email", email,
            "confirmationLink", confirmationUrl,
            "firstName", firstName != null ? firstName : "Member"
        );

        return sendEmail(
            email,
            EmailTemplateType.EMAIL_TEMPLATE_EMAIL_CONFIRMATION,
            "Confirm your email address - EcclesiaFlow",
            variables,
            Priority.PRIORITY_HIGH,
            EmailOperation.CONFIRMATION
        );
    }

    @SuppressWarnings("unused")
    private UUID sendConfirmationEmailFallback(String email, String confirmationUrl, String firstName, Throwable t) {
        throw new EmailServiceUnavailableException(SERVICE_NAME, t);
    }

    @Override
    @CircuitBreaker(name = ResilienceConfig.EMAIL_SERVICE_CB, fallbackMethod = "sendWelcomeEmailFallback")
    @Retry(name = ResilienceConfig.EMAIL_SERVICE_RETRY)
    public UUID sendWelcomeEmail(String email, String firstName) {
        Map<String, String> variables = Map.of(
            "email", email,
            "firstName", firstName != null ? firstName : "Member"
        );

        return sendEmail(
            email,
            EmailTemplateType.EMAIL_TEMPLATE_WELCOME,
            "Welcome to EcclesiaFlow",
            variables,
            Priority.PRIORITY_NORMAL,
            EmailOperation.WELCOME
        );
    }

    @SuppressWarnings("unused")
    private UUID sendWelcomeEmailFallback(String email, String firstName, Throwable t) {
        throw new EmailServiceUnavailableException(SERVICE_NAME, t);
    }

    private UUID sendEmail(String toEmail,
                          EmailTemplateType templateType,
                          String subject,
                          Map<String, String> variables,
                          Priority priority,
                          EmailOperation operation) {
        try {
            SendEmailRequest request = SendEmailRequest.newBuilder()
                    .addTo(toEmail)
                    .setSubject(subject)
                    .setTemplateType(templateType)
                    .putAllVariables(variables)
                    .setPriority(priority)
                    .build();

            SendEmailResponse response = stub.sendEmail(request);
            return UUID.fromString(response.getEmailId());
            
        } catch (StatusRuntimeException e) {
            throw mapToEmailServiceException(e, toEmail, operation);
        }
    }

    private EmailServiceException mapToEmailServiceException(StatusRuntimeException e,
                                                           String toEmail,
                                                           EmailOperation operation) {
        Status.Code statusCode = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        GrpcCommunicationException grpcException = new GrpcCommunicationException(
                SERVICE_NAME,
                "sendEmail",
                statusCode,
                description != null ? description : "No description provided",
                e
        );

        return new EmailServiceException(
                String.format("Email sending failed (%s): %s", operation.name(), statusCode),
                toEmail,
                operation,
                grpcException
        );
    }
}
