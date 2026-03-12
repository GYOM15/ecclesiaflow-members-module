package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.security.RequireScopes;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.mappers.SignUpRequestMapper;
import com.ecclesiaflow.web.mappers.UpdateRequestMapper;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpRequestPayload;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.UpdateMemberRequestPayload;
import com.ecclesiaflow.web.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Delegate for member management operations (CRUD + /me routes).
 *
 * <p>Separates HTTP concerns (controller) from business logic. Each public
 * method maps to an OpenAPI endpoint and is guarded by {@code @RequireScopes}.</p>
 */
@Service
@RequiredArgsConstructor
public class MembersManagementDelegate {

    private final MemberService memberService;
    private final UpdateRequestMapper updateRequestMapper;
    private final OpenApiModelMapper openApiModelMapper;
    private final AuthenticatedUserService authenticatedUserService;

    /** Registers a new member (email confirmation flow). */
    public ResponseEntity<SignUpResponse> createMember(SignUpRequestPayload signUpRequestPayload) {
        MembershipRegistration registration = SignUpRequestMapper.fromSignUpRequest(signUpRequestPayload);

        Member member = memberService.registerMember(registration);

        SignUpResponse response = openApiModelMapper.createSignUpResponse(member, "Member registered (temporary - approval system coming)");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Returns a paginated, optionally filtered list of all members. */
    @RequireScopes("ef:members:read:all")
    public ResponseEntity<MemberPageResponse> getAllMembers(
            Integer page, Integer size, String search, String status, String sort, String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);

        MemberStatus memberStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                memberStatus = MemberStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status — ignore the filter
            }
        }

        Page<Member> memberPage = memberService.getAllMembers(pageable, search, memberStatus);

        MemberPageResponse response = openApiModelMapper.createMemberPageResponse(memberPage);

        return ResponseEntity.ok(response);
    }

    /** Retrieves a single member by ID. */
    @RequireScopes({"ef:members:read:own", "ef:members:read:all"})
    public ResponseEntity<SignUpResponse> getMemberById(UUID memberId) {
        Member member = memberService.findByMemberId(memberId);
        SignUpResponse response = openApiModelMapper.createSignUpResponse(member, "Member found");

        return ResponseEntity.ok(response);
    }

    /** Partially updates a member's profile. */
    @RequireScopes({"ef:members:write:own", "ef:members:write:all"})
    public ResponseEntity<SignUpResponse> updateMemberPartially(UUID memberId, UpdateMemberRequestPayload updateMemberRequestPayload) {
        MembershipUpdate businessRequest = updateRequestMapper.fromUpdateMemberRequest(memberId, updateMemberRequestPayload);

        Member updatedMember = memberService.updateMember(businessRequest);

        SignUpResponse response = openApiModelMapper.createSignUpResponse(updatedMember, "Member updated");

        return ResponseEntity.ok(response);
    }

    /** Soft-deletes a member (sets DEACTIVATED status, disables Keycloak login). */
    @RequireScopes({"ef:members:delete:own", "ef:members:delete:all"})
    public ResponseEntity<Void> deleteMember(UUID memberId) {
        memberService.deactivateMember(memberId);

        return ResponseEntity.noContent().build();
    }

    // --- /me routes (authenticated member) ---

    /** Returns the authenticated member's profile. */
    @RequireScopes("ef:members:read:own")
    public ResponseEntity<SignUpResponse> getMyProfile() {
        String keycloakUserId = authenticatedUserService.getKeycloakUserId();

        Member member = memberService.getByKeycloakUserId(keycloakUserId);
        SignUpResponse response = openApiModelMapper.createSignUpResponse(member, "Profile retrieved");

        return ResponseEntity.ok(response);
    }

    /** Updates the authenticated member's profile. */
    @RequireScopes("ef:members:write:own")
    public ResponseEntity<SignUpResponse> updateMyProfile(UpdateMemberRequestPayload updateMemberRequestPayload) {
        String keycloakUserId = authenticatedUserService.getKeycloakUserId();
        Member member = memberService.getByKeycloakUserId(keycloakUserId);

        MembershipUpdate businessRequest = updateRequestMapper.fromUpdateMemberRequest(member.getMemberId(), updateMemberRequestPayload);
        Member updatedMember = memberService.updateMember(businessRequest);
        SignUpResponse response = openApiModelMapper.createSignUpResponse(updatedMember, "Profile updated");

        return ResponseEntity.ok(response);
    }

    /** Soft-deletes the authenticated member's account. */
    @RequireScopes("ef:members:delete:own")
    public ResponseEntity<Void> deleteMyAccount() {
        String keycloakUserId = authenticatedUserService.getKeycloakUserId();
        Member member = memberService.getByKeycloakUserId(keycloakUserId);

        memberService.deactivateMember(member.getMemberId());

        return ResponseEntity.noContent().build();
    }

    // --- Private helpers ---

    private Pageable createPageable(Integer page, Integer size, String sort, String direction) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String sortField = sort != null ? sort : "firstName";
        String sortDirection = direction != null ? direction : "asc";

        Sort.Direction dir = "desc".equalsIgnoreCase(sortDirection)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        return PageRequest.of(pageNumber, pageSize, Sort.by(dir, sortField));
    }
}
