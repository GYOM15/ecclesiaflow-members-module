package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.business.domain.MembershipUpdate;
import com.ecclesiaflow.web.mappers.persistence.MemberUpdateMapper;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.entities.Role;
import com.ecclesiaflow.web.dto.SignUpRequest;
import com.ecclesiaflow.web.dto.UpdateMemberRequest;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests unitaires pour MembersController.
 * Vérifie les endpoints REST de gestion des membres.
 */
@WebMvcTest(MembersController.class)
class MembersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberUpdateMapper memberUpdateMapper;

    private Member testMember;
    private SignUpRequest signUpRequest;
    private UpdateMemberRequest updateRequest;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();

        testMember = new Member();
        testMember.setId(testMemberId);
        testMember.setMemberId(UUID.randomUUID());
        testMember.setFirstName("Jean");
        testMember.setLastName("Dupont");
        testMember.setEmail("jean.dupont@example.com");
        testMember.setAddress("123 Rue de la Paix");
        testMember.setRole(Role.MEMBER);
        testMember.setConfirmed(true);
        testMember.setCreatedAt(LocalDateTime.now());

        signUpRequest = new SignUpRequest();
        signUpRequest.setFirstName("Jean");
        signUpRequest.setLastName("Dupont");
        signUpRequest.setEmail("jean.dupont@example.com");
        signUpRequest.setAddress("123 Rue de la Paix");

        updateRequest = new UpdateMemberRequest();
        updateRequest.setFirstName("Jean-Updated");
        updateRequest.setLastName("Dupont-Updated");
        updateRequest.setEmail("jean.updated@example.com");
        updateRequest.setAddress("456 Nouvelle Adresse");
    }

    @Test
    void sayHello_ShouldReturnHelloMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/ecclesiaflow/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hi Member"));
    }

    @Test
    void signUp_WithValidData_ShouldReturnCreatedMember() throws Exception {
        // Given
        MembershipRegistration registration = new MembershipRegistration(
                "Jean", "Dupont", "jean.dupont@example.com", "123 Rue de la Paix"
        );

        when(memberService.registerMember(any(MembershipRegistration.class))).thenReturn(testMember);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Member registered (temporary - approval system coming)"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.email").value("jean.dupont@example.com"))
                .andExpect(jsonPath("$.address").value("123 Rue de la Paix"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(memberService).registerMember(any(MembershipRegistration.class));
    }

    @Test
    void signUp_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        SignUpRequest invalidRequest = new SignUpRequest();
        // Tous les champs requis sont null

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).registerMember(any());
    }

    @Test
    void signUp_WithExistingEmail_ShouldReturnConflict() throws Exception {
        // Given
        when(memberService.registerMember(any(MembershipRegistration.class)))
                .thenThrow(new IllegalArgumentException("Un compte avec cet email existe déjà."));

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Un compte avec cet email existe déjà."));
    }

    @Test
    void getMemberById_WithExistingId_ShouldReturnMember() throws Exception {
        // Given
        when(memberService.findById(testMemberId)).thenReturn(testMember);

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members/{id}", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Membre trouvé"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.email").value("jean.dupont@example.com"));

        verify(memberService).findById(testMemberId);
    }

    @Test
    void getMemberById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(memberService.findById(nonExistentId))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre non trouvé"));
    }

    @Test
    void updateMember_WithValidData_ShouldReturnUpdatedMember() throws Exception {
        // Given
        MembershipUpdate membershipUpdate = MembershipUpdate.builder()
                .memberId(testMemberId)
                .firstName("Jean-Updated")
                .lastName("Dupont-Updated")
                .email("jean.updated@example.com")
                .address("456 Nouvelle Adresse")
                .build();

        when(memberUpdateMapper.fromUpdateMemberRequest(testMemberId, updateRequest))
                .thenReturn(membershipUpdate);
        when(memberService.updateMember(membershipUpdate)).thenReturn(testMember);

        // When & Then
        mockMvc.perform(patch("/ecclesiaflow/members/{id}", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Membre modifié avec succès"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"));

        verify(memberUpdateMapper).fromUpdateMemberRequest(testMemberId, updateRequest);
        verify(memberService).updateMember(membershipUpdate);
    }

    @Test
    void updateMember_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        MembershipUpdate membershipUpdate = MembershipUpdate.builder()
                .memberId(nonExistentId)
                .build();

        when(memberUpdateMapper.fromUpdateMemberRequest(nonExistentId, updateRequest))
                .thenReturn(membershipUpdate);
        when(memberService.updateMember(membershipUpdate))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When & Then
        mockMvc.perform(patch("/ecclesiaflow/members/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre non trouvé"));
    }

    @Test
    void deleteMember_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(memberService).deleteMember(testMemberId);

        // When & Then
        mockMvc.perform(delete("/ecclesiaflow/members/{id}", testMemberId))
                .andExpect(status().isNoContent());

        verify(memberService).deleteMember(testMemberId);
    }

    @Test
    void deleteMember_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new MemberNotFoundException("Membre non trouvé"))
                .when(memberService).deleteMember(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/ecclesiaflow/members/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre non trouvé"));
    }

    @Test
    void getAllMembers_ShouldReturnMembersList() throws Exception {
        // Given
        Member member2 = new Member();
        member2.setId(UUID.randomUUID());
        member2.setEmail("member2@example.com");
        member2.setFirstName("Marie");
        member2.setLastName("Martin");
        member2.setRole(Role.MEMBER);
        member2.setConfirmed(false);
        member2.setCreatedAt(LocalDateTime.now());

        List<Member> members = Arrays.asList(testMember, member2);
        when(memberService.getAllMembers()).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Jean"))
                .andExpect(jsonPath("$[1].firstName").value("Marie"));

        verify(memberService).getAllMembers();
    }

    @Test
    void getAllMembers_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(memberService.getAllMembers()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void signUp_WithMissingRequiredFields_ShouldReturnValidationErrors() throws Exception {
        // Given
        SignUpRequest invalidRequest = new SignUpRequest();
        invalidRequest.setFirstName(""); // Champ vide
        invalidRequest.setEmail("invalid-email"); // Email invalide
        // lastName et address manquants

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation des données"))
                .andExpect(jsonPath("$.errors").isArray());

        verify(memberService, never()).registerMember(any());
    }

    @Test
    void getMemberConfirmationStatus_WithConfirmedMember_ShouldReturnTrue() throws Exception {
        // Given
        String email = "jean.dupont@example.com";
        when(memberService.isEmailConfirmed(email)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members/{email}/confirmation-status", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true));
    }

    @Test
    void getMemberConfirmationStatus_WithUnconfirmedMember_ShouldReturnFalse() throws Exception {
        // Given
        String email = "unconfirmed@example.com";
        when(memberService.isEmailConfirmed(email)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members/{email}/confirmation-status", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(false));
    }

    @Test
    void getMemberConfirmationStatus_WithNonExistentMember_ShouldReturnNotFound() throws Exception {
        // Given
        String email = "non-existent@example.com";
        when(memberService.isEmailConfirmed(email))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When & Then
        mockMvc.perform(get("/ecclesiaflow/members/{email}/confirmation-status", email))
                .andExpect(status().isNotFound());
    }
}