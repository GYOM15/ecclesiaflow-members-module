package com.ecclesiaflow.business.domain.member;

/**
 * Enum representing the lifecycle status of a member.
 */
public enum MemberStatus {
    /**
     * Member has registered but not yet confirmed their email.
     */
    PENDING,

    /**
     * Member has confirmed their email but not yet set their password.
     */
    CONFIRMED,

    /**
     * Member has set their password and account is fully active.
     */
    ACTIVE,

    /**
     * Member account has been suspended.
     */
    SUSPENDED,

    /**
     * Member account has been deactivated.
     */
    INACTIVE
}
