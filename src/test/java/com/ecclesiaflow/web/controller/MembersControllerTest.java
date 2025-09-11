package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.domain.member.Role;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import com.ecclesiaflow.web.exception.InvalidRequestException; // Supposons cette exception pour email déjà utilisé
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.payloads.SignUpRequestPayload;
import com.ecclesiaflow.web.dto.SignUpResponse;
import com.ecclesiaflow.web.payloads.UpdateMemberRequestPayload;
import com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler;
import com.ecclesiaflow.web.mappers.UpdateRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper; // Pour sérialiser les DTOs dans le corps des requêtes

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
    private ObjectMapper objectMapper; // Pour convertir les objets en JSON

    @Mock
    private MemberService memberService;

    @Mock
    private UpdateRequestMapper updateRequestMapper;

    // Les mappers sont des classes statiques, pas besoin de les mocker

    @InjectMocks
    private MembersController membersController;

    private final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(membersController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Décommentez si vous avez un GlobalExceptionHandler
                .build();
        objectMapper = new ObjectMapper();
    }

    // --- Tests pour /ecclesiaflow/hello ---
    @Test
    void sayHello_shouldReturnMessageAndCorrectContentType() throws Exception {
        mockMvc.perform(get("/ecclesiaflow/hello")
                        .accept("application/vnd.ecclesiaflow.members.v1+json")) // Spécifiez le type accepté
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(content().string("Hi Member"));
    }

    // --- Tests pour POST /ecclesiaflow/members (registerMember) ---
    @Test
    void registerMember_shouldReturnCreated() throws Exception {
        // Préparation de la requête
        SignUpRequestPayload request = new SignUpRequestPayload();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@mail.com");
        request.setAddress("123 Main St");
        
        MembershipRegistration registration = new MembershipRegistration("John", "Doe", "john.doe@mail.com", "123 Main St",null );
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@mail.com")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .build();

        String expectedMessage = "Member registered (temporary - approval system coming)";
        SignUpResponse responseDto = SignUpResponse.builder()
                .email("john.doe@mail.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .role("MEMBER")
                .confirmed(false)
                .createdAt(member.getCreatedAt().format(ISO_FORMATTER))
                .message(expectedMessage)
                .build();

        // Définir les comportements des mocks
        when(memberService.registerMember(any(MembershipRegistration.class))).thenReturn(member);

        // Exécuter la requête et vérifier les résultats
        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.email").value("john.doe@mail.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.confirmed").value(false));

        // Vérifier que les méthodes mockées ont été appelées
        verify(memberService).registerMember(any(MembershipRegistration.class));
    }

    @Test
    void registerMember_shouldReturnBadRequestForInvalidInput() throws Exception {
        // Requête avec email invalide (sera attrapé par @Valid)
        SignUpRequestPayload invalidRequest = new SignUpRequestPayload();
        invalidRequest.setFirstName("J"); // Trop court
        invalidRequest.setLastName("Doe");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setAddress("123 Street");

        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists()); // Vérifiez qu'un champ d'erreur est présent
    }

    @Test
    void registerMember_shouldReturnBadRequestIfEmailAlreadyUsed() throws Exception {
        SignUpRequestPayload request = new SignUpRequestPayload();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@mail.com");
        request.setAddress("456 Boulevard des Lilas");
        request.setPhoneNumber("+1234567890");

        MembershipRegistration registration = new MembershipRegistration("Jane", "Doe", "jane.doe@mail.com", "456 Boulevard des Lilas","+1234567890" );

        when(memberService.registerMember(any(MembershipRegistration.class)))
                .thenThrow(new InvalidRequestException("Email already in use")); // Simulez l'exception métier

        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Ou .isConflict() si votre GlobalExceptionHandler le mappe ainsi
                .andExpect(jsonPath("$.message").value("Email already in use"));

        verify(memberService).registerMember(any(MembershipRegistration.class));
    }

    // --- Tests pour GET /ecclesiaflow/members/{memberId} (getMember) ---
    @Test
    void getMember_shouldReturnMember() throws Exception {
        UUID id = UUID.randomUUID();
        Member member = Member.builder()
                .memberId(id)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@mail.com")
                .role(Role.MEMBER)
                .createdAt(LocalDateTime.now())
                .confirmed(true)
                .build();

        String expectedMessage = "Membre trouvé";
        SignUpResponse responseDto = SignUpResponse.builder()
                .email("jane.doe@mail.com")
                .firstName("Jane")
                .lastName("Doe")
                .role("MEMBER")
                .confirmed(true)
                .createdAt(member.getCreatedAt().format(ISO_FORMATTER))
                .message(expectedMessage)
                .build();

        when(memberService.findById(id)).thenReturn(member);

        mockMvc.perform(get("/ecclesiaflow/members/" + id)
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.email").value("jane.doe@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(memberService).findById(id);
    }

    @Test
    void getMember_shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(memberService.findById(id)).thenThrow(new MemberNotFoundException("Membre non trouvé avec ID: " + id));

        mockMvc.perform(get("/ecclesiaflow/members/" + id)
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre non trouvé avec ID: " + id));

        verify(memberService).findById(id);
    }

    // --- Tests pour PATCH /ecclesiaflow/members/{memberId} (updateMember) ---
    @Test
    void updateMember_shouldReturnUpdatedMember() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMemberRequestPayload updateRequest = new UpdateMemberRequestPayload();
        updateRequest.setFirstName("NewName");
        updateRequest.setEmail("new.email@mail.com");

        MembershipUpdate businessUpdate = MembershipUpdate.builder()
                .memberId(id)
                .firstName("NewName")
                .email("new.email@mail.com")
                .build(); // Exemple simplifié, à compléter selon votre logique

        Member updatedMember = Member.builder()
                .memberId(id)
                .firstName("NewName")
                .lastName("Doe")
                .email("new.email@mail.com")
                .role(Role.MEMBER)
                .createdAt(LocalDateTime.now().minusDays(1))
                .confirmed(true)
                .build();

        String expectedMessage = "Membre modifié avec succès";
        SignUpResponse responseDto = SignUpResponse.builder()
                .email("new.email@mail.com")
                .firstName("NewName")
                .lastName("Doe")
                .role("MEMBER")
                .confirmed(true)
                .createdAt(updatedMember.getCreatedAt().format(ISO_FORMATTER))
                .message(expectedMessage)
                .build();

        when(updateRequestMapper.fromUpdateMemberRequest(eq(id), any(UpdateMemberRequestPayload.class))).thenReturn(businessUpdate);
        when(memberService.updateMember(any(MembershipUpdate.class))).thenReturn(updatedMember);

        mockMvc.perform(patch("/ecclesiaflow/members/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.firstName").value("NewName"))
                .andExpect(jsonPath("$.email").value("new.email@mail.com"))
                .andExpect(jsonPath("$.message").value(expectedMessage));

        verify(updateRequestMapper).fromUpdateMemberRequest(eq(id), any(UpdateMemberRequestPayload.class));
        verify(memberService).updateMember(any(MembershipUpdate.class));
    }

    @Test
    void updateMember_shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMemberRequestPayload updateRequest = new UpdateMemberRequestPayload();
        updateRequest.setFirstName("NewName");

        MembershipUpdate businessUpdate = MembershipUpdate.builder().memberId(id).firstName("NewName").build();

        when(updateRequestMapper.fromUpdateMemberRequest(eq(id), any(UpdateMemberRequestPayload.class))).thenReturn(businessUpdate);
        when(memberService.updateMember(any(MembershipUpdate.class)))
                .thenThrow(new MemberNotFoundException("Membre à mettre à jour non trouvé avec ID: " + id));

        mockMvc.perform(patch("/ecclesiaflow/members/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre à mettre à jour non trouvé avec ID: " + id));

        verify(updateRequestMapper).fromUpdateMemberRequest(eq(id), any(UpdateMemberRequestPayload.class));
        verify(memberService).updateMember(any(MembershipUpdate.class));
    }

    @Test
    void updateMember_shouldReturnBadRequestForInvalidInput() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMemberRequestPayload invalidRequest = new UpdateMemberRequestPayload();
        invalidRequest.setFirstName("AveryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongFirstName"); // Plus de 50 caractères

        mockMvc.perform(patch("/ecclesiaflow/members/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Mapper statique ne nécessite pas de vérification
        verifyNoInteractions(memberService);
    }


    // --- Tests pour GET /ecclesiaflow/members (getAllMembers) ---
    @Test
    void getAllMembers_shouldReturnListOfMembers() throws Exception {
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("Alice").email("alice@mail.com").role(Role.MEMBER).createdAt(LocalDateTime.now()).confirmed(true).build(),
                Member.builder().memberId(UUID.randomUUID()).firstName("Bob").email("bob@mail.com").role(Role.MEMBER).createdAt(LocalDateTime.now()).confirmed(false).build()
        );

        when(memberService.getAllMembers()).thenReturn(members);

        mockMvc.perform(get("/ecclesiaflow/members")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$[0].email").value("alice@mail.com"))
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[1].email").value("bob@mail.com"));

        verify(memberService).getAllMembers();
    }

    @Test
    void getAllMembers_shouldReturnEmptyList() throws Exception {
        when(memberService.getAllMembers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/ecclesiaflow/members")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$").isEmpty()); // Vérifie que la liste est vide

        verify(memberService).getAllMembers();
    }


    // --- Tests pour GET /ecclesiaflow/members/{email}/confirmation-status ---
    @Test
    void getMemberConfirmationStatus_shouldReturnTrue() throws Exception {
        String email = "confirmed@mail.com";
        when(memberService.isEmailConfirmed(email)).thenReturn(true);

        mockMvc.perform(get("/ecclesiaflow/members/" + email + "/confirmation-status")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(memberService).isEmailConfirmed(email);
    }

    @Test
    void getMemberConfirmationStatus_shouldReturnFalse() throws Exception {
        String email = "unconfirmed@mail.com";
        when(memberService.isEmailConfirmed(email)).thenReturn(false);

        mockMvc.perform(get("/ecclesiaflow/members/" + email + "/confirmation-status")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.confirmed").value(false));

        verify(memberService).isEmailConfirmed(email);
    }

    // Le contrôleur ne devrait pas retourner 404 ici, le service doit toujours retourner true/false
    // Si le service lance MemberNotFoundException, le contrôleur devrait le gérer.
    // Supposons que isEmailConfirmed gère déjà l'email non trouvé en retournant false.
    // Si isEmailConfirmed peut lancer une MemberNotFoundException, il faudrait tester ce cas aussi.
    @Test
    void getMemberConfirmationStatus_shouldReturnNotFoundIfMemberServiceThrowsNotFound() throws Exception {
        String email = "nonexistent@mail.com";
        when(memberService.isEmailConfirmed(email)).thenThrow(new MemberNotFoundException("Membre non trouvé"));

        mockMvc.perform(get("/ecclesiaflow/members/" + email + "/confirmation-status")
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre non trouvé"));

        verify(memberService).isEmailConfirmed(email);
    }


    // --- Tests pour DELETE /ecclesiaflow/members/{memberId} (deleteMember) ---
    @Test
    void deleteMember_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(memberService).deleteMember(id); // Simuler une suppression réussie

        mockMvc.perform(delete("/ecclesiaflow/members/" + id)
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isNoContent());

        verify(memberService).deleteMember(id); // Vérifier que le service a été appelé
    }

    @Test
    void deleteMember_shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new MemberNotFoundException("Membre à supprimer non trouvé avec ID: " + id))
                .when(memberService).deleteMember(id);

        mockMvc.perform(delete("/ecclesiaflow/members/" + id)
                        .accept("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre à supprimer non trouvé avec ID: " + id));

        verify(memberService).deleteMember(id);
    }
}