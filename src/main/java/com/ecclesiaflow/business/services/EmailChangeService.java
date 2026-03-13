package com.ecclesiaflow.business.services;

import com.ecclesiaflow.business.domain.member.Member;

import java.util.UUID;

/** Manages email change requests with re-verification. */
public interface EmailChangeService {

    void requestEmailChange(UUID memberId, String newEmail);

    Member confirmEmailChange(UUID token);
}
