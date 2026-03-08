package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.web.exception.InvalidRequestException;
import com.ecclesiaflow.web.model.SignUpRequestPayload;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.UpdateMemberRequestPayload;
import com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.delegate.MembersManagementDelegate;
import com.ecclesiaflow.web.delegate.MembersTemporaryDelegate;
import com.ecclesiaflow.web.delegate.SocialOnboardingDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MembersControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MembersManagementDelegate membersManagementDelegate;

    @Mock
    private MembersTemporaryDelegate membersTemporaryDelegate;

    @Mock
    private SocialOnboardingDelegate socialOnboardingDelegate;

    @InjectMocks
    private MembersController membersController;

    private final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        membersController = new MembersController(
                membersManagementDelegate, membersTemporaryDelegate, socialOnboardingDelegate);
        mockMvc = MockMvcBuilders.standaloneSetup(membersController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    // --- Tests for /ecclesiaflow/hello ---
    @Test
    void sayHello_shouldReturnMessageAndCorrectContentType() throws Exception {
        when(membersTemporaryDelegate.sayHello())
                .thenReturn(ResponseEntity.ok().body("Hi Member"));

        mockMvc.perform(get("/ecclesiaflow/hello")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Hi Member"));

        verify(membersTemporaryDelegate).sayHello();
    }

    // --- Tests for POST /ecclesiaflow/members (registerMember) ---
    @Test
    void registerMember_shouldReturnCreated() throws Exception {
        SignUpRequestPayload request = new SignUpRequestPayload();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@mail.com");
        request.setAddress("123 Main St");

        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@mail.com")
                .status(MemberStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        String expectedMessage = "Member registered (temporary - approval system coming)";
        SignUpResponse responseDto = new SignUpResponse()
                .email("john.doe@mail.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .confirmed(false)
                .createdAt(member.getCreatedAt().format(ISO_FORMATTER))
                .message(expectedMessage);

        when(membersManagementDelegate.createMember(any(SignUpRequestPayload.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.email").value("john.doe@mail.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.confirmed").value(false));

        verify(membersManagementDelegate).createMember(any(SignUpRequestPayload.class));
    }

    @Test
    void registerMember_shouldReturnBadRequestForInvalidInput() throws Exception {
        SignUpRequestPayload invalidRequest = new SignUpRequestPayload();
        invalidRequest.setFirstName("J"); // Too short
        invalidRequest.setLastName("Doe");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setAddress("123 Street");

        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void registerMember_shouldReturnBadRequestIfEmailAlreadyUsed() throws Exception {
        SignUpRequestPayload request = new SignUpRequestPayload();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@mail.com");
        request.setAddress("456 Boulevard des Lilas");
        request.setPhoneNumber("+1234567890");

        when(membersManagementDelegate.createMember(any(SignUpRequestPayload.class)))
                .thenThrow(new InvalidRequestException("Email already in use"));

        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already in use"));

        verify(membersManagementDelegate).createMember(any(SignUpRequestPayload.class));
    }

    // --- Tests for GET /ecclesiaflow/members/{memberId} (getMember) ---
    @Test
    void getMember_shouldReturnMember() throws Exception {
        UUID id = UUID.randomUUID();
        Member member = Member.builder()
                .memberId(id)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@mail.com")
                .status(MemberStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        String expectedMessage = "Member found";
        SignUpResponse responseDto = new SignUpResponse()
                .email("jane.doe@mail.com")
                .firstName("Jane")
                .lastName("Doe")
                .confirmed(true)
                .createdAt(member.getCreatedAt().format(ISO_FORMATTER))
                .message(expectedMessage);

        when(membersManagementDelegate.getMemberById(id))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/ecclesiaflow/members/" + id)
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.email").value("jane.doe@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(membersManagementDelegate).getMemberById(id);
    }

    @Test
    void getMember_shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(membersManagementDelegate.getMemberById(id))
                .thenThrow(new MemberNotFoundException("Member not found with ID: " + id));

        mockMvc.perform(get("/ecclesiaflow/members/" + id)
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Member not found with ID: " + id));

        verify(membersManagementDelegate).getMemberById(id);
    }

    // --- Tests for PATCH /ecclesiaflow/members/{memberId} (updateMember) ---
    @Test
    void updateMember_shouldReturnUpdatedMember() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMemberRequestPayload updateRequest = new UpdateMemberRequestPayload();
        updateRequest.setFirstName("NewName");
        updateRequest.setEmail("new.email@mail.com");

        Member updatedMember = Member.builder()
                .memberId(id)
                .firstName("NewName")
                .lastName("Doe")
                .email("new.email@mail.com")
                .status(MemberStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        String expectedMessage = "Member updated";
        SignUpResponse responseDto = new SignUpResponse()
                .email("new.email@mail.com")
                .firstName("NewName")
                .lastName("Doe")
                .confirmed(true)
                .createdAt(updatedMember.getCreatedAt().format(ISO_FORMATTER))
                .message(expectedMessage);

        when(membersManagementDelegate.updateMemberPartially(eq(id), any(UpdateMemberRequestPayload.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/ecclesiaflow/members/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.firstName").value("NewName"))
                .andExpect(jsonPath("$.email").value("new.email@mail.com"))
                .andExpect(jsonPath("$.message").value(expectedMessage));

        verify(membersManagementDelegate).updateMemberPartially(eq(id), any(UpdateMemberRequestPayload.class));
    }

    @Test
    void updateMember_shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMemberRequestPayload updateRequest = new UpdateMemberRequestPayload();
        updateRequest.setFirstName("NewName");

        when(membersManagementDelegate.updateMemberPartially(eq(id), any(UpdateMemberRequestPayload.class)))
                .thenThrow(new MemberNotFoundException("Member not found with ID: " + id));

        mockMvc.perform(patch("/ecclesiaflow/members/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Member not found with ID: " + id));

        verify(membersManagementDelegate).updateMemberPartially(eq(id), any(UpdateMemberRequestPayload.class));
    }

    @Test
    void updateMember_shouldReturnBadRequestForInvalidInput() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMemberRequestPayload invalidRequest = new UpdateMemberRequestPayload();
        invalidRequest.setFirstName("AveryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongFirstName");

        mockMvc.perform(patch("/ecclesiaflow/members/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(membersManagementDelegate);
    }


    // --- Tests for GET /ecclesiaflow/members (getAllMembers) with pagination ---
    @Test
    void getAllMembers_shouldReturnPageOfMembers() throws Exception {
        List<SignUpResponse> signUpResponses = List.of(
                new SignUpResponse().email("alice@mail.com").firstName("Alice"),
                new SignUpResponse().email("bob@mail.com").firstName("Bob")
        );

        MemberPageResponse pageResponse = new MemberPageResponse()
                .content(signUpResponses)
                .totalElements(2L)
                .totalPages(1)
                .size(20)
                .number(0)
                .page(0);

        when(membersManagementDelegate.getAllMembers(eq(0), eq(20), eq(null), eq(null), any(), any()))
                .thenReturn(ResponseEntity.ok(pageResponse));

        mockMvc.perform(get("/ecclesiaflow/members")
                        .param("page", "0")
                        .param("size", "20")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.content[0].email").value("alice@mail.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.content[1].email").value("bob@mail.com"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(membersManagementDelegate).getAllMembers(eq(0), eq(20), eq(null), eq(null), any(), any());
    }

    @Test
    void getAllMembers_shouldReturnEmptyPage() throws Exception {
        MemberPageResponse emptyPageResponse = new MemberPageResponse()
                .content(Collections.emptyList())
                .totalElements(0L)
                .totalPages(0)
                .size(20)
                .number(0);

        when(membersManagementDelegate.getAllMembers(eq(0), eq(20), eq(null), eq(null), any(), any()))
                .thenReturn(ResponseEntity.ok(emptyPageResponse));

        mockMvc.perform(get("/ecclesiaflow/members")
                        .param("page", "0")
                        .param("size", "20")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(membersManagementDelegate).getAllMembers(eq(0), eq(20), eq(null), eq(null), any(), any());
    }

    @Test
    void getAllMembers_shouldReturnMembersWithSearchTerm() throws Exception {
        List<SignUpResponse> signUpResponses = List.of(
                new SignUpResponse().email("alice@mail.com").firstName("Alice")
        );

        MemberPageResponse pageResponse = new MemberPageResponse()
                .content(signUpResponses)
                .totalElements(1L)
                .totalPages(1)
                .size(20)
                .number(0);

        when(membersManagementDelegate.getAllMembers(eq(0), eq(20), eq("alice"), eq(null), any(), any()))
                .thenReturn(ResponseEntity.ok(pageResponse));

        mockMvc.perform(get("/ecclesiaflow/members")
                        .param("search", "alice")
                        .param("page", "0")
                        .param("size", "20")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(membersManagementDelegate).getAllMembers(eq(0), eq(20), eq("alice"), eq(null), any(), any());
    }

    @Test
    void getAllMembers_shouldReturnMembersWithStatusFilter() throws Exception {
        List<SignUpResponse> signUpResponses = List.of(
                new SignUpResponse().email("alice@mail.com").firstName("Alice")
        );

        MemberPageResponse pageResponse = new MemberPageResponse()
                .content(signUpResponses)
                .totalElements(1L)
                .totalPages(1)
                .size(20)
                .number(0);

        when(membersManagementDelegate.getAllMembers(eq(0), eq(20), eq(null), eq("ACTIVE"), any(), any()))
                .thenReturn(ResponseEntity.ok(pageResponse));

        mockMvc.perform(get("/ecclesiaflow/members")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "20")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(membersManagementDelegate).getAllMembers(eq(0), eq(20), eq(null), eq("ACTIVE"), any(), any());
    }


    // --- Tests for GET /ecclesiaflow/members/{email}/confirmation-status ---
    @Test
    void getMemberConfirmationStatus_shouldReturnTrue() throws Exception {
        String email = "confirmed@mail.com";
        com.ecclesiaflow.web.model.MemberConfirmationStatusResponse response = new com.ecclesiaflow.web.model.MemberConfirmationStatusResponse();
        response.setConfirmed(true);
        when(membersTemporaryDelegate.getMemberConfirmationStatus(email))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/ecclesiaflow/members/" + email + "/confirmation-status")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(membersTemporaryDelegate).getMemberConfirmationStatus(email);
    }

    @Test
    void getMemberConfirmationStatus_shouldReturnFalse() throws Exception {
        String email = "unconfirmed@mail.com";
        com.ecclesiaflow.web.model.MemberConfirmationStatusResponse response = new com.ecclesiaflow.web.model.MemberConfirmationStatusResponse();
        response.setConfirmed(false);
        when(membersTemporaryDelegate.getMemberConfirmationStatus(email))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/ecclesiaflow/members/" + email + "/confirmation-status")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.confirmed").value(false));

        verify(membersTemporaryDelegate).getMemberConfirmationStatus(email);
    }

    @Test
    void getMemberConfirmationStatus_shouldReturnNotFoundIfMemberServiceThrowsNotFound() throws Exception {
        String email = "nonexistent@mail.com";
        when(membersTemporaryDelegate.getMemberConfirmationStatus(email))
                .thenThrow(new MemberNotFoundException("Member not found"));

        mockMvc.perform(get("/ecclesiaflow/members/" + email + "/confirmation-status")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Member not found"));

        verify(membersTemporaryDelegate).getMemberConfirmationStatus(email);
    }


    // --- Tests for DELETE /ecclesiaflow/members/{memberId} (deleteMember) ---
    @Test
    void deleteMember_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        when(membersManagementDelegate.deleteMember(id))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/ecclesiaflow/members/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(membersManagementDelegate).deleteMember(id);
    }

    // --- Tests for GET /ecclesiaflow/members/me (getMyProfile) ---
    @Test
    void getMyProfile_shouldReturnAuthenticatedMemberProfile() throws Exception {
        SignUpResponse response = new SignUpResponse();
        response.setEmail("test@example.com");
        response.setFirstName("Test");
        response.setLastName("User");
        response.setMessage("Profile retrieved");

        when(membersManagementDelegate.getMyProfile())
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/ecclesiaflow/members/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(membersManagementDelegate).getMyProfile();
    }

    // --- Tests for PATCH /ecclesiaflow/members/me (updateMyProfile) ---
    @Test
    void updateMyProfile_shouldReturnUpdatedProfile() throws Exception {
        UpdateMemberRequestPayload updateRequest = new UpdateMemberRequestPayload();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");

        SignUpResponse response = new SignUpResponse();
        response.setEmail("test@example.com");
        response.setFirstName("Updated");
        response.setLastName("Name");
        response.setMessage("Profile updated");

        when(membersManagementDelegate.updateMyProfile(any(UpdateMemberRequestPayload.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(patch("/ecclesiaflow/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));

        verify(membersManagementDelegate).updateMyProfile(any(UpdateMemberRequestPayload.class));
    }

    // --- Tests for DELETE /ecclesiaflow/members/me (deleteMyAccount) ---
    @Test
    void deleteMyAccount_shouldReturnNoContent() throws Exception {
        when(membersManagementDelegate.deleteMyAccount())
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/ecclesiaflow/members/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(membersManagementDelegate).deleteMyAccount();
    }

}
