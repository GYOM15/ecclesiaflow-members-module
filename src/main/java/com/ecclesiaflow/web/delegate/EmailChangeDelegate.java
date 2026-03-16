package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.services.EmailChangeService;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.EmailChangeResponse;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.UpdateEmailRequestPayload;
import com.ecclesiaflow.web.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Delegate for email change endpoints. */
@Service
@RequiredArgsConstructor
public class EmailChangeDelegate {

    private final EmailChangeService emailChangeService;
    private final MemberService memberService;
    private final AuthenticatedUserService authenticatedUserService;
    private final OpenApiModelMapper openApiModelMapper;

    public ResponseEntity<EmailChangeResponse> requestEmailChange(UpdateEmailRequestPayload payload) {
        String keycloakUserId = authenticatedUserService.getKeycloakUserId();
        Member member = memberService.getByKeycloakUserId(keycloakUserId);

        emailChangeService.requestEmailChange(member.getMemberId(), payload.getEmail());

        return ResponseEntity.accepted()
                .body(new EmailChangeResponse().message("Confirmation email sent to new address"));
    }

    public ResponseEntity<SignUpResponse> confirmEmailChange(UUID token) {
        Member updated = emailChangeService.confirmEmailChange(token);
        return ResponseEntity.ok(openApiModelMapper.createSignUpResponse(updated, "Email updated"));
    }
}
