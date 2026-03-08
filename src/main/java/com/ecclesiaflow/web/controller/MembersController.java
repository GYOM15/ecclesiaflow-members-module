package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.web.api.MembersManagementApi;
import com.ecclesiaflow.web.api.MembersTemporaryApi;
import com.ecclesiaflow.web.api.SocialOnboardingApi;
import com.ecclesiaflow.web.delegate.MembersManagementDelegate;
import com.ecclesiaflow.web.delegate.MembersTemporaryDelegate;
import com.ecclesiaflow.web.delegate.SocialOnboardingDelegate;
import com.ecclesiaflow.web.model.MemberConfirmationStatusResponse;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpRequestPayload;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.SocialOnboardingRequest;
import com.ecclesiaflow.web.model.SocialOnboardingResponse;
import com.ecclesiaflow.web.model.UpdateMemberRequestPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller that implements OpenAPI-generated interfaces and delegates
 * all business logic to specialized delegate classes.
 */
@RestController
@RequiredArgsConstructor
public class MembersController implements MembersManagementApi, MembersTemporaryApi, SocialOnboardingApi {

    private final MembersManagementDelegate membersManagementDelegate;
    private final MembersTemporaryDelegate membersTemporaryDelegate;
    private final SocialOnboardingDelegate socialOnboardingDelegate;

    // --- SocialOnboardingApi ---

    @Override
    public ResponseEntity<SocialOnboardingResponse> _membersSocialOnboarding(
            SocialOnboardingRequest socialOnboardingRequest) {
        return socialOnboardingDelegate.socialOnboarding(socialOnboardingRequest);
    }

    // --- MembersTemporaryApi ---

    @Override
    public ResponseEntity<String> _membersSayHello() {
        return membersTemporaryDelegate.sayHello();
    }

    @Override
    public ResponseEntity<MemberConfirmationStatusResponse> _membersGetConfirmationStatus(String email) {
        return membersTemporaryDelegate.getMemberConfirmationStatus(email);
    }

    // --- MembersManagementApi ---

    @Override
    public ResponseEntity<SignUpResponse> _membersCreate(SignUpRequestPayload signUpRequestPayload) {
        return membersManagementDelegate.createMember(signUpRequestPayload);
    }

    @Override
    public ResponseEntity<MemberPageResponse> _membersGetAll(
            Integer page, Integer size, @Nullable String search,
            @Nullable String status, String sort, String direction) {
        return membersManagementDelegate.getAllMembers(page, size, search, status, sort, direction);
    }

    @Override
    public ResponseEntity<SignUpResponse> _membersGetById(UUID memberId) {
        return membersManagementDelegate.getMemberById(memberId);
    }

    @Override
    public ResponseEntity<SignUpResponse> _membersUpdatePartially(UUID memberId, UpdateMemberRequestPayload updateMemberRequestPayload) {
        return membersManagementDelegate.updateMemberPartially(memberId, updateMemberRequestPayload);
    }

    @Override
    public ResponseEntity<Void> _membersDelete(UUID memberId) {
        return membersManagementDelegate.deleteMember(memberId);
    }

    // --- /me routes ---

    @Override
    public ResponseEntity<SignUpResponse> _membersGetMyProfile() {
        return membersManagementDelegate.getMyProfile();
    }

    @Override
    public ResponseEntity<SignUpResponse> _membersUpdateMyProfile(UpdateMemberRequestPayload updateMemberRequestPayload) {
        return membersManagementDelegate.updateMyProfile(updateMemberRequestPayload);
    }

    @Override
    public ResponseEntity<Void> _membersDeleteMyAccount() {
        return membersManagementDelegate.deleteMyAccount();
    }
}
