package com.ecclesiaflow.business.domain.events;

/**
 * Domain event published after successful member account activation.
 * <p>
 * This event is triggered when a member completes password setup and their
 * account status changes from PENDING to ACTIVE. It is used to trigger
 * post-activation actions asynchronously after transaction commit.
 * </p>
 *
 * <p><strong>Architectural role:</strong> Domain Event - Event-Driven Architecture</p>
 *
 * @param email     Email address of the activated member.
 * @param firstName First name of the member for email personalization.
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
public record MemberActivatedEvent(String email, String firstName) {
}
