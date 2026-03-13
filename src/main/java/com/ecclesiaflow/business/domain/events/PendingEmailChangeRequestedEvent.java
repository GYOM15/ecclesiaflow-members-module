package com.ecclesiaflow.business.domain.events;

import java.util.UUID;

public record PendingEmailChangeRequestedEvent(String newEmail, UUID token, String firstName) {
}
