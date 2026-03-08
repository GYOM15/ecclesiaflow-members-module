package com.ecclesiaflow.application.handlers;

import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.events.MemberActivatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for member account activation.
 * 
 * <p>This class listens to {@link MemberActivatedEvent} business events
 * and orchestrates the necessary technical actions (sending welcome email via gRPC).</p>
 *
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 * @see MemberActivatedEvent
 * @see EmailClient
 * @see TransactionalEventListener
 */
@Component
@RequiredArgsConstructor
public class MemberActivationEventHandler {

    private final EmailClient emailClient;

    /**
     * Handles the member activation event.
     *
     * @param event event containing email and firstName of the activated member
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailTaskExecutor")
    public void handleMemberActivated(MemberActivatedEvent event) {
        try {
            emailClient.sendWelcomeEmail(event.email(), event.firstName());
        } catch (Exception e) {
            // Exception captured to not impact the business transaction
            // Logging is handled by AOP aspects
        }
    }
}
