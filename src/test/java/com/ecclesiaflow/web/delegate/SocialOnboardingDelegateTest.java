package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.SocialProvider;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.exception.InvalidRequestException;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.SocialOnboardingRequest;
import com.ecclesiaflow.web.model.SocialOnboardingResponse;
import com.ecclesiaflow.web.security.AuthenticatedUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialOnboardingDelegate")
class SocialOnboardingDelegateTest {

    @Mock
    private MemberService memberService;

    @Mock
    private OpenApiModelMapper openApiModelMapper;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private SocialOnboardingDelegate delegate;

    private SocialOnboardingRequest buildRequest(String email) {
        SocialOnboardingRequest request = new SocialOnboardingRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(email);
        request.setAddress("123 Main St");
        request.setPhoneNumber("+1234567890");
        return request;
    }

    @Nested
    @DisplayName("socialOnboarding - success")
    class Success {

        @Test
        @DisplayName("should create member with Google provider")
        void shouldCreateMemberWithGoogle() {
            SocialOnboardingRequest request = buildRequest("user@gmail.com");
            Member member = Member.builder()
                    .memberId(UUID.randomUUID())
                    .email("user@gmail.com")
                    .firstName("John")
                    .status(MemberStatus.ACTIVE)
                    .build();
            SocialOnboardingResponse response = new SocialOnboardingResponse();

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("user@gmail.com"));
            when(authenticatedUserService.getIdentityProvider()).thenReturn(Optional.of("google"));
            when(memberService.registerSocialMember(eq("kc-123"), eq(SocialProvider.GOOGLE), any(MembershipRegistration.class)))
                    .thenReturn(member);
            when(openApiModelMapper.createSocialOnboardingResponse(member)).thenReturn(response);

            ResponseEntity<SocialOnboardingResponse> result = delegate.socialOnboarding(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("should create member with Facebook provider")
        void shouldCreateMemberWithFacebook() {
            SocialOnboardingRequest request = buildRequest("user@fb.com");
            Member member = Member.builder().memberId(UUID.randomUUID()).email("user@fb.com").build();
            SocialOnboardingResponse response = new SocialOnboardingResponse();

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("user@fb.com"));
            when(authenticatedUserService.getIdentityProvider()).thenReturn(Optional.of("facebook"));
            when(memberService.registerSocialMember(eq("kc-123"), eq(SocialProvider.FACEBOOK), any())).thenReturn(member);
            when(openApiModelMapper.createSocialOnboardingResponse(member)).thenReturn(response);

            ResponseEntity<SocialOnboardingResponse> result = delegate.socialOnboarding(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("should create member with Microsoft provider")
        void shouldCreateMemberWithMicrosoft() {
            SocialOnboardingRequest request = buildRequest("user@outlook.com");
            Member member = Member.builder().memberId(UUID.randomUUID()).email("user@outlook.com").build();
            SocialOnboardingResponse response = new SocialOnboardingResponse();

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("user@outlook.com"));
            when(authenticatedUserService.getIdentityProvider()).thenReturn(Optional.of("microsoft"));
            when(memberService.registerSocialMember(eq("kc-123"), eq(SocialProvider.MICROSOFT), any())).thenReturn(member);
            when(openApiModelMapper.createSocialOnboardingResponse(member)).thenReturn(response);

            ResponseEntity<SocialOnboardingResponse> result = delegate.socialOnboarding(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("should handle unknown provider as null")
        void shouldHandleUnknownProvider() {
            SocialOnboardingRequest request = buildRequest("user@example.com");
            Member member = Member.builder().memberId(UUID.randomUUID()).email("user@example.com").build();
            SocialOnboardingResponse response = new SocialOnboardingResponse();

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("user@example.com"));
            when(authenticatedUserService.getIdentityProvider()).thenReturn(Optional.of("unknown-idp"));
            when(memberService.registerSocialMember(eq("kc-123"), eq(null), any())).thenReturn(member);
            when(openApiModelMapper.createSocialOnboardingResponse(member)).thenReturn(response);

            ResponseEntity<SocialOnboardingResponse> result = delegate.socialOnboarding(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("should handle missing identity provider as null")
        void shouldHandleMissingProvider() {
            SocialOnboardingRequest request = buildRequest("user@example.com");
            Member member = Member.builder().memberId(UUID.randomUUID()).email("user@example.com").build();
            SocialOnboardingResponse response = new SocialOnboardingResponse();

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("user@example.com"));
            when(authenticatedUserService.getIdentityProvider()).thenReturn(Optional.empty());
            when(memberService.registerSocialMember(eq("kc-123"), eq(null), any())).thenReturn(member);
            when(openApiModelMapper.createSocialOnboardingResponse(member)).thenReturn(response);

            ResponseEntity<SocialOnboardingResponse> result = delegate.socialOnboarding(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("should accept case-insensitive email match")
        void shouldAcceptCaseInsensitiveEmail() {
            SocialOnboardingRequest request = buildRequest("User@Gmail.COM");
            Member member = Member.builder().memberId(UUID.randomUUID()).email("User@Gmail.COM").build();
            SocialOnboardingResponse response = new SocialOnboardingResponse();

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("user@gmail.com"));
            when(authenticatedUserService.getIdentityProvider()).thenReturn(Optional.of("google"));
            when(memberService.registerSocialMember(any(), any(), any())).thenReturn(member);
            when(openApiModelMapper.createSocialOnboardingResponse(member)).thenReturn(response);

            ResponseEntity<SocialOnboardingResponse> result = delegate.socialOnboarding(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("socialOnboarding - validation errors")
    class ValidationErrors {

        @Test
        @DisplayName("should throw when JWT has no email claim")
        void shouldThrowWhenNoEmailClaim() {
            SocialOnboardingRequest request = buildRequest("user@gmail.com");

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> delegate.socialOnboarding(request))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("email claim");
        }

        @Test
        @DisplayName("should throw when request email doesn't match JWT")
        void shouldThrowWhenEmailMismatch() {
            SocialOnboardingRequest request = buildRequest("attacker@evil.com");

            when(authenticatedUserService.getKeycloakUserId()).thenReturn("kc-123");
            when(authenticatedUserService.getEmail()).thenReturn(Optional.of("real@gmail.com"));

            assertThatThrownBy(() -> delegate.socialOnboarding(request))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("does not match");
        }
    }
}
