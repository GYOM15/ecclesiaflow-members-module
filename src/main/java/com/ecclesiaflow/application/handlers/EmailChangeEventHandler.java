package com.ecclesiaflow.application.handlers;

import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.events.EmailChangedEvent;
import com.ecclesiaflow.business.domain.events.PendingEmailChangeRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Sends emails on email change request and confirmation. */
@Component
@RequiredArgsConstructor
public class EmailChangeEventHandler {

    private final EmailClient emailClient;

    @Value("${ecclesiaflow.frontend.base-url}")
    private String frontendBaseUrl;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailTaskExecutor")
    public void handleEmailChangeRequested(PendingEmailChangeRequestedEvent event) {
        try {
            String url = frontendBaseUrl + "/email-change-confirmation?token=" + event.token();
            emailClient.sendConfirmationEmail(event.newEmail(), url, event.firstName());
        } catch (Exception e) {
            // Logged by AOP — do not impact the business transaction
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailTaskExecutor")
    public void handleEmailChanged(EmailChangedEvent event) {
        try {
            emailClient.sendEmailChangedNotification(event.oldEmail(), event.firstName());
        } catch (Exception e) {
            // Logged by AOP — do not impact the business transaction
        }
    }
}
