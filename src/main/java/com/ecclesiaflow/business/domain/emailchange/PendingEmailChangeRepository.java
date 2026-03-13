package com.ecclesiaflow.business.domain.emailchange;

import java.util.Optional;
import java.util.UUID;

public interface PendingEmailChangeRepository {

    PendingEmailChange save(PendingEmailChange pendingChange);

    Optional<PendingEmailChange> getByToken(UUID token);

    Optional<PendingEmailChange> getByMemberId(UUID memberId);

    void delete(PendingEmailChange pendingChange);

    void deleteByMemberId(UUID memberId);
}
