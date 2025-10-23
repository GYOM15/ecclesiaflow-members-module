package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.domain.member.Role;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.security.AuthenticatedUserContextProvider;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.mappers.UpdateRequestMapper;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpRequestPayload;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.UpdateMemberRequestPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembersManagementDelegateTest {

    @Mock
    private MemberService memberService;

    @Mock
    private UpdateRequestMapper updateRequestMapper;

    @Mock
    private OpenApiModelMapper openApiModelMapper;

    @Mock
    private AuthenticatedUserContextProvider contextProvider;

    @InjectMocks
    private MembersManagementDelegate membersManagementDelegate;

    // --- Tests for createMember ---
    @Test
    void createMember_shouldReturnCreatedResponse() {
        // Given
        SignUpRequestPayload requestPayload = new SignUpRequestPayload();
        requestPayload.setFirstName("John");
        requestPayload.setLastName("Doe");
        requestPayload.setEmail("john.doe@example.com");
        requestPayload.setAddress("123 Main St");

        Member createdMember = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .build();

        SignUpResponse expectedResponse = new SignUpResponse()
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("MEMBER")
                .confirmed(false)
                .message("Member registered (temporary - approval system coming)");

        when(memberService.registerMember(any(MembershipRegistration.class)))
                .thenReturn(createdMember);
        when(openApiModelMapper.createSignUpResponse(eq(createdMember), anyString()))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<SignUpResponse> response = membersManagementDelegate.createMember(requestPayload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getBody().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().getConfirmed()).isFalse();

        verify(memberService).registerMember(any(MembershipRegistration.class));
        verify(openApiModelMapper).createSignUpResponse(eq(createdMember), eq("Member registered (temporary - approval system coming)"));
    }

    // --- Tests for getAllMembers ---
    @Test
    void getAllMembers_shouldReturnPageOfMembers() {
        // Given
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("Alice").email("alice@example.com").role(Role.MEMBER).createdAt(LocalDateTime.now()).confirmed(true).build(),
                Member.builder().memberId(UUID.randomUUID()).firstName("Bob").email("bob@example.com").role(Role.MEMBER).createdAt(LocalDateTime.now()).confirmed(false).build()
        );

        Page<Member> memberPage = new PageImpl<>(members, PageRequest.of(0, 20), 2);

        MemberPageResponse expectedResponse = new MemberPageResponse()
                .content(List.of(
                        new SignUpResponse().email("alice@example.com").firstName("Alice"),
                        new SignUpResponse().email("bob@example.com").firstName("Bob")
                ))
                .totalElements(2L)
                .totalPages(1)
                .size(20)
                .number(0);

        when(memberService.getAllMembers(any(Pageable.class), eq(null), eq(null)))
                .thenReturn(memberPage);
        when(openApiModelMapper.createMemberPageResponse(memberPage))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<MemberPageResponse> response = membersManagementDelegate.getAllMembers(0, 20, null, null, "firstName", "asc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(2L);
        assertThat(response.getBody().getContent()).hasSize(2);

        verify(memberService).getAllMembers(any(Pageable.class), eq(null), eq(null));
        verify(openApiModelMapper).createMemberPageResponse(memberPage);
    }

    @Test
    void getAllMembers_shouldHandleSearchParameter() {
        // Given
        Page<Member> memberPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        MemberPageResponse expectedResponse = new MemberPageResponse()
                .content(Collections.emptyList())
                .totalElements(0L);

        when(memberService.getAllMembers(any(Pageable.class), eq("alice"), eq(null)))
                .thenReturn(memberPage);
        when(openApiModelMapper.createMemberPageResponse(memberPage))
                .thenReturn(expectedResponse);

        // When
        membersManagementDelegate.getAllMembers(0, 20, "alice", null, "firstName", "asc");

        // Then
        verify(memberService).getAllMembers(any(Pageable.class), eq("alice"), eq(null));
    }

    @Test
    void getAllMembers_shouldHandleConfirmationFilter() {
        // Given
        Page<Member> memberPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        MemberPageResponse expectedResponse = new MemberPageResponse()
                .content(Collections.emptyList())
                .totalElements(0L);

        when(memberService.getAllMembers(any(Pageable.class), eq(null), eq(true)))
                .thenReturn(memberPage);
        when(openApiModelMapper.createMemberPageResponse(memberPage))
                .thenReturn(expectedResponse);

        // When
        membersManagementDelegate.getAllMembers(0, 20, null, true, "firstName", "asc");

        // Then
        verify(memberService).getAllMembers(any(Pageable.class), eq(null), eq(true));
    }

    @Test
    void getAllMembers_shouldHandleDescendingSort() {
        // Given - Test de la branche DESC dans createPageable()
        Page<Member> memberPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(memberService.getAllMembers(any(Pageable.class), eq(null), eq(null)))
                .thenReturn(memberPage);
        when(openApiModelMapper.createMemberPageResponse(memberPage))
                .thenReturn(new MemberPageResponse());

        // When - direction = "desc"
        ResponseEntity<MemberPageResponse> response = membersManagementDelegate.getAllMembers(
                0, 20, null, null, "lastName", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(memberService).getAllMembers(any(Pageable.class), eq(null), eq(null));
    }

    @Test
    void getAllMembers_shouldHandleCustomPageSize() {
        // Given - Test avec des valeurs personnalisées (non null)
        Page<Member> memberPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 50), 0);
        when(memberService.getAllMembers(any(Pageable.class), eq(null), eq(null)))
                .thenReturn(memberPage);
        when(openApiModelMapper.createMemberPageResponse(memberPage))
                .thenReturn(new MemberPageResponse());

        // When - page=2, size=50
        ResponseEntity<MemberPageResponse> response = membersManagementDelegate.getAllMembers(
                2, 50, null, null, "email", "asc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(memberService).getAllMembers(any(Pageable.class), eq(null), eq(null));
    }

    @Test
    void getAllMembers_shouldHandleAllNullParameters() {
        // Given - Test avec tous les paramètres null (valeurs par défaut)
        Page<Member> memberPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(memberService.getAllMembers(any(Pageable.class), eq(null), eq(null)))
                .thenReturn(memberPage);
        when(openApiModelMapper.createMemberPageResponse(memberPage))
                .thenReturn(new MemberPageResponse());

        // When - Tous les paramètres sont null
        ResponseEntity<MemberPageResponse> response = membersManagementDelegate.getAllMembers(
                null, null, null, null, null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(memberService).getAllMembers(any(Pageable.class), eq(null), eq(null));
    }

    // --- Tests for getMemberById ---
    @Test
    void getMemberById_shouldReturnMember() {
        // Given
        UUID memberId = UUID.randomUUID();
        Member member = Member.builder()
                .memberId(memberId)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .role(Role.MEMBER)
                .confirmed(true)
                .createdAt(LocalDateTime.now())
                .build();

        SignUpResponse expectedResponse = new SignUpResponse()
                .email("jane.doe@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .role("MEMBER")
                .confirmed(true)
                .message("Membre trouvé");

        when(memberService.findByMemberId(memberId)).thenReturn(member);
        when(openApiModelMapper.createSignUpResponse(member, "Membre trouvé"))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<SignUpResponse> response = membersManagementDelegate.getMemberById(memberId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(response.getBody().getConfirmed()).isTrue();

        verify(memberService).findByMemberId(memberId);
        verify(openApiModelMapper).createSignUpResponse(member, "Membre trouvé");
    }

    @Test
    void getMemberById_shouldPropagateMemberNotFoundException() {
        // Given
        UUID memberId = UUID.randomUUID();
        when(memberService.findByMemberId(memberId))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When/Then
        assertThatThrownBy(() -> membersManagementDelegate.getMemberById(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Membre non trouvé");

        verify(memberService).findByMemberId(memberId);
        verifyNoInteractions(openApiModelMapper);
    }

    // --- Tests for updateMemberPartially ---
    @Test
    void updateMemberPartially_shouldReturnUpdatedMember() {
        // Given
        UUID memberId = UUID.randomUUID();
        UpdateMemberRequestPayload requestPayload = new UpdateMemberRequestPayload();
        requestPayload.setFirstName("NewName");
        requestPayload.setEmail("new.email@example.com");

        MembershipUpdate businessUpdate = MembershipUpdate.builder()
                .memberId(memberId)
                .firstName("NewName")
                .email("new.email@example.com")
                .build();

        Member updatedMember = Member.builder()
                .memberId(memberId)
                .firstName("NewName")
                .lastName("Doe")
                .email("new.email@example.com")
                .role(Role.MEMBER)
                .confirmed(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        SignUpResponse expectedResponse = new SignUpResponse()
                .email("new.email@example.com")
                .firstName("NewName")
                .lastName("Doe")
                .role("MEMBER")
                .confirmed(true)
                .message("Membre modifié avec succès");

        when(updateRequestMapper.fromUpdateMemberRequest(memberId, requestPayload))
                .thenReturn(businessUpdate);
        when(memberService.updateMember(businessUpdate))
                .thenReturn(updatedMember);
        when(openApiModelMapper.createSignUpResponse(updatedMember, "Membre modifié avec succès"))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<SignUpResponse> response = membersManagementDelegate.updateMemberPartially(memberId, requestPayload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("NewName");
        assertThat(response.getBody().getEmail()).isEqualTo("new.email@example.com");

        verify(updateRequestMapper).fromUpdateMemberRequest(memberId, requestPayload);
        verify(memberService).updateMember(businessUpdate);
        verify(openApiModelMapper).createSignUpResponse(updatedMember, "Membre modifié avec succès");
    }

    @Test
    void updateMemberPartially_shouldPropagateMemberNotFoundException() {
        // Given
        UUID memberId = UUID.randomUUID();
        UpdateMemberRequestPayload requestPayload = new UpdateMemberRequestPayload();
        requestPayload.setFirstName("NewName");

        MembershipUpdate businessUpdate = MembershipUpdate.builder()
                .memberId(memberId)
                .firstName("NewName")
                .build();

        when(updateRequestMapper.fromUpdateMemberRequest(memberId, requestPayload))
                .thenReturn(businessUpdate);
        when(memberService.updateMember(businessUpdate))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When/Then
        assertThatThrownBy(() -> membersManagementDelegate.updateMemberPartially(memberId, requestPayload))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Membre non trouvé");

        verify(updateRequestMapper).fromUpdateMemberRequest(memberId, requestPayload);
        verify(memberService).updateMember(businessUpdate);
        verifyNoInteractions(openApiModelMapper);
    }

    // --- Tests for deleteMember ---
    @Test
    void deleteMember_shouldReturnNoContent() {
        // Given
        UUID memberId = UUID.randomUUID();
        doNothing().when(memberService).deleteMember(memberId);

        // When
        ResponseEntity<Void> response = membersManagementDelegate.deleteMember(memberId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(memberService).deleteMember(memberId);
    }

    @Test
    void deleteMember_shouldPropagateMemberNotFoundException() {
        // Given
        UUID memberId = UUID.randomUUID();
        doThrow(new MemberNotFoundException("Membre non trouvé"))
                .when(memberService).deleteMember(memberId);

        // When/Then
        assertThatThrownBy(() -> membersManagementDelegate.deleteMember(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Membre non trouvé");

        verify(memberService).deleteMember(memberId);
    }

    @Test
    void getMyProfile_shouldReturnAuthenticatedMemberProfile() {
        // Given
        UUID memberId = UUID.randomUUID();
        Member member = Member.builder()
                .memberId(memberId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .address("123 Test St")
                .role(Role.MEMBER)
                .confirmed(true)
                .createdAt(LocalDateTime.now())
                .build();

        SignUpResponse response = new SignUpResponse();
        response.setEmail("test@example.com");
        response.setFirstName("Test");
        response.setLastName("User");

        when(contextProvider.getAuthenticatedMemberId()).thenReturn(memberId);
        when(memberService.findByMemberId(memberId)).thenReturn(member);
        when(openApiModelMapper.createSignUpResponse(member, "Profil récupéré")).thenReturn(response);

        // When
        ResponseEntity<SignUpResponse> result = membersManagementDelegate.getMyProfile();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getEmail()).isEqualTo("test@example.com");

        verify(contextProvider).getAuthenticatedMemberId();
        verify(memberService).findByMemberId(memberId);
        verify(openApiModelMapper).createSignUpResponse(member, "Profil récupéré");
    }

    @Test
    void updateMyProfile_shouldUpdateAuthenticatedMemberProfile() {
        // Given
        UUID memberId = UUID.randomUUID();
        UpdateMemberRequestPayload updatePayload = new UpdateMemberRequestPayload();
        updatePayload.setFirstName("Updated");
        updatePayload.setLastName("Name");

        MembershipUpdate membershipUpdate = MembershipUpdate.builder()
                .memberId(memberId)
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .address("New Address")
                .phoneNumber("+1234567890")
                .build();

        Member updatedMember = Member.builder()
                .memberId(memberId)
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .address("New Address")
                .role(Role.MEMBER)
                .confirmed(true)
                .build();

        SignUpResponse response = new SignUpResponse();
        response.setEmail("updated@example.com");

        when(contextProvider.getAuthenticatedMemberId()).thenReturn(memberId);
        when(updateRequestMapper.fromUpdateMemberRequest(memberId, updatePayload)).thenReturn(membershipUpdate);
        when(memberService.updateMember(membershipUpdate)).thenReturn(updatedMember);
        when(openApiModelMapper.createSignUpResponse(updatedMember, "Profil mis à jour")).thenReturn(response);

        // When
        ResponseEntity<SignUpResponse> result = membersManagementDelegate.updateMyProfile(updatePayload);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        verify(contextProvider).getAuthenticatedMemberId();
        verify(updateRequestMapper).fromUpdateMemberRequest(memberId, updatePayload);
        verify(memberService).updateMember(membershipUpdate);
    }

    @Test
    void deleteMyAccount_shouldDeleteAuthenticatedMemberAccount() {
        // Given
        UUID memberId = UUID.randomUUID();
        when(contextProvider.getAuthenticatedMemberId()).thenReturn(memberId);
        doNothing().when(memberService).deleteMember(memberId);

        // When
        ResponseEntity<Void> result = membersManagementDelegate.deleteMyAccount();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(contextProvider).getAuthenticatedMemberId();
        verify(memberService).deleteMember(memberId);
    }
}
