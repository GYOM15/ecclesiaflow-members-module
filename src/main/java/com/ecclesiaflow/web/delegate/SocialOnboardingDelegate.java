package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.security.RequireScopes;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.exception.InvalidRequestException;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.SocialOnboardingRequest;
import com.ecclesiaflow.web.model.SocialOnboardingResponse;
import com.ecclesiaflow.web.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Delegate for the social onboarding endpoint.
 *
 * Handles profile creation for users who authenticated via a social provider
 * (Google, Facebook) through Keycloak. The member is created with ACTIVE status
 * and linked to the Keycloak user ID extracted from the Bearer JWT.
 */
@Service
@RequiredArgsConstructor
public class SocialOnboardingDelegate {

    private final MemberService memberService;
    private final OpenApiModelMapper openApiModelMapper;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Creates a member profile for a socially-authenticated user.
     *
     * <p>Validates that the email in the request body matches the JWT email claim
     * to prevent profile creation for a different user. The member is created
     * directly as ACTIVE (no email confirmation needed since the social provider
     * already verified the email).</p>
     *
     * @param request social onboarding data from the frontend form
     * @return 201 Created with the new member profile
     * @throws InvalidRequestException if the request email doesn't match the JWT email
     */
    @RequireScopes("ef:members:write:own")
    public ResponseEntity<SocialOnboardingResponse> socialOnboarding(SocialOnboardingRequest request) {
        String keycloakUserId = authenticatedUserService.getKeycloakUserId();
        String jwtEmail = authenticatedUserService.getEmail()
                .orElseThrow(() -> new InvalidRequestException(
                        "JWT does not contain an email claim"));

        validateEmailMatchesJwt(request.getEmail(), jwtEmail);

        MembershipRegistration registration = new MembershipRegistration(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getAddress(),
                request.getPhoneNumber()
        );

        Member member = memberService.registerSocialMember(keycloakUserId, registration);

        SocialOnboardingResponse response = openApiModelMapper
                .createSocialOnboardingResponse(member);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validateEmailMatchesJwt(String requestEmail, String jwtEmail) {
        if (!jwtEmail.equalsIgnoreCase(requestEmail)) {
            throw new InvalidRequestException(
                    "Request email does not match the authenticated user's email");
        }
    }
}
