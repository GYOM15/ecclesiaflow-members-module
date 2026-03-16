package com.ecclesiaflow.business.domain.member;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Partial update request for an existing member profile.
 * Null fields are left unchanged; non-null fields replace the current value.
 */
@Getter
@Builder(toBuilder = true)
public class MembershipUpdate {

    /** Required — identifies the member to update. */
    private UUID memberId;

    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
}
