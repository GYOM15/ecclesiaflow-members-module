package com.ecclesiaflow.business.domain.emailchange;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class PendingEmailChange {

    private final UUID id;
    private final UUID memberId;
    private final String newEmail;
    private final UUID token;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
