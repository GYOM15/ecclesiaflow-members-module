package com.ecclesiaflow.business.services;


import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.MembershipConfirmation;

import java.util.UUID;

public interface MemberConfirmationService {
    MembershipConfirmationResult confirmMember(MembershipConfirmation confirmationRequest);
    void sendConfirmationCode(UUID memberId);
}
