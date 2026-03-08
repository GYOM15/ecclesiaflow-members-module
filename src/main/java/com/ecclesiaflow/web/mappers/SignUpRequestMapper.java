package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.web.model.SignUpRequestPayload;

/** Static mapper: OpenAPI SignUpRequestPayload to domain MembershipRegistration. */
public class SignUpRequestMapper {

    /** Converts a sign-up request DTO to a domain registration object. */
    public static MembershipRegistration fromSignUpRequest(SignUpRequestPayload req) {
        return new MembershipRegistration(
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getAddress(),
                req.getPhoneNumber()
        );
    }
}
