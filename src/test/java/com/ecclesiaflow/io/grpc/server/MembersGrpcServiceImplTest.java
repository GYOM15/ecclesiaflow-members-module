package com.ecclesiaflow.io.grpc.server;

import com.ecclesiaflow.business.domain.events.MemberActivatedEvent;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import org.springframework.context.ApplicationEventPublisher;
import com.ecclesiaflow.grpc.members.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link MembersGrpcServiceImpl}.
 * <p>
 * Vérifie le traitement des requêtes gRPC entrantes pour les statuts de confirmation.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MembersGrpcServiceImpl - Tests du service gRPC Members")
class MembersGrpcServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StreamObserver<ConfirmationStatusResponse> responseObserver;

    @InjectMocks
    private MembersGrpcServiceImpl service;

    @Captor
    private ArgumentCaptor<ConfirmationStatusResponse> responseCaptor;

    @Captor
    private ArgumentCaptor<StatusRuntimeException> errorCaptor;

    private static final String VALID_EMAIL = "test@example.com";
    private static final UUID MEMBER_ID = UUID.randomUUID();

    private Member confirmedMember;
    private Member unconfirmedMember;

    @BeforeEach
    void setUp() {
        confirmedMember = Member.builder()
                .memberId(MEMBER_ID)
                .email(VALID_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .confirmedAt(LocalDateTime.now())
                .status(MemberStatus.ACTIVE)
                .build();

        unconfirmedMember = Member.builder()
                .memberId(MEMBER_ID)
                .email(VALID_EMAIL)
                .firstName("Jane")
                .lastName("Smith")
                .confirmedAt(null)
                .status(MemberStatus.PENDING)
                .build();
    }

    // ========================================================================
    // Tests - Membre confirmé
    // ========================================================================

    @Test
    @DisplayName("Doit retourner memberExists=true et isConfirmed=true pour un membre confirmé")
    void getMemberConfirmationStatus_ConfirmedMember() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(VALID_EMAIL)
                .build();
        when(memberRepository.getByEmail(VALID_EMAIL)).thenReturn(Optional.of(confirmedMember));

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        ConfirmationStatusResponse response = responseCaptor.getValue();
        assertThat(response.getMemberExists()).isTrue();
        assertThat(response.getIsConfirmed()).isTrue();
    }

    // ========================================================================
    // Tests - Membre non confirmé
    // ========================================================================

    @Test
    @DisplayName("Doit retourner memberExists=true et isConfirmed=false pour un membre non confirmé")
    void getMemberConfirmationStatus_UnconfirmedMember() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(VALID_EMAIL)
                .build();
        when(memberRepository.getByEmail(VALID_EMAIL)).thenReturn(Optional.of(unconfirmedMember));

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ConfirmationStatusResponse response = responseCaptor.getValue();
        assertThat(response.getMemberExists()).isTrue();
        assertThat(response.getIsConfirmed()).isFalse();
    }

    // ========================================================================
    // Tests - Membre inexistant
    // ========================================================================

    @Test
    @DisplayName("Doit retourner memberExists=false pour un membre inexistant")
    void getMemberConfirmationStatus_MemberNotFound() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("nonexistent@example.com")
                .build();
        when(memberRepository.getByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ConfirmationStatusResponse response = responseCaptor.getValue();
        assertThat(response.getMemberExists()).isFalse();
        assertThat(response.getIsConfirmed()).isFalse();
    }

    // ========================================================================
    // Tests - Validation d'email
    // ========================================================================

    @Test
    @DisplayName("Doit rejeter un email vide avec INVALID_ARGUMENT")
    void getMemberConfirmationStatus_EmptyEmail() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("")
                .build();

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(error.getStatus().getDescription()).contains("Email cannot be empty");
    }

    @Test
    @DisplayName("Doit rejeter un email avec seulement des espaces")
    void getMemberConfirmationStatus_BlankEmail() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("   ")
                .build();

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(error.getStatus().getDescription()).contains("Email cannot be empty");
    }

    @Test
    @DisplayName("Doit rejeter un email sans @")
    void getMemberConfirmationStatus_InvalidEmailNoAt() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("invalidexample.com")
                .build();

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(error.getStatus().getDescription()).contains("Invalid email format");
    }

    @Test
    @DisplayName("Doit rejeter un email sans domaine")
    void getMemberConfirmationStatus_InvalidEmailNoDomain() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("test@")
                .build();

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(error.getStatus().getDescription()).contains("Invalid email format");
    }

    @Test
    @DisplayName("Doit rejeter un email sans extension")
    void getMemberConfirmationStatus_InvalidEmailNoExtension() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("test@example")
                .build();

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());
        assertThat(error.getStatus().getDescription()).contains("Invalid email format");
    }

    // ========================================================================
    // Tests - Gestion d'erreurs génériques
    // ========================================================================

    @Test
    @DisplayName("Doit retourner INTERNAL pour une exception inattendue")
    void getMemberConfirmationStatus_UnexpectedException() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(VALID_EMAIL)
                .build();
        when(memberRepository.getByEmail(VALID_EMAIL))
                .thenThrow(new RuntimeException("Database connection lost"));

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INTERNAL.getCode());
        assertThat(error.getStatus().getDescription()).contains("unexpected error occurred");
    }

    @Test
    @DisplayName("Doit gérer NullPointerException comme INTERNAL")
    void getMemberConfirmationStatus_NullPointerException() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(VALID_EMAIL)
                .build();
        when(memberRepository.getByEmail(VALID_EMAIL))
                .thenThrow(new NullPointerException("Null member data"));

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.INTERNAL.getCode());
    }

    // ========================================================================
    // Tests - Cas limites
    // ========================================================================

    @Test
    @DisplayName("Doit accepter un email avec caractères spéciaux valides")
    void getMemberConfirmationStatus_EmailWithSpecialChars() {
        // Given
        String specialEmail = "user+test_123@example.co.uk";
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(specialEmail)
                .build();
        when(memberRepository.getByEmail(specialEmail)).thenReturn(Optional.empty());

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onNext(any());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    @DisplayName("Doit accepter un email avec tirets")
    void getMemberConfirmationStatus_EmailWithDashes() {
        // Given
        String emailWithDash = "user-name@my-domain.com";
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(emailWithDash)
                .build();
        when(memberRepository.getByEmail(emailWithDash)).thenReturn(Optional.empty());

        // When
        service.getMemberConfirmationStatus(request, responseObserver);

        // Then
        verify(responseObserver).onNext(any());
        verify(responseObserver).onCompleted();
    }

    // ========================================================================
    // Tests - notifyAccountActivated
    // ========================================================================

    @Nested
    @DisplayName("notifyAccountActivated")
    class NotifyAccountActivated {

        @Mock
        private StreamObserver<AccountActivatedResponse> activatedObserver;

        @Captor
        private ArgumentCaptor<AccountActivatedResponse> activatedCaptor;

        @Test
        @DisplayName("should activate confirmed member successfully")
        void shouldActivateConfirmedMember() {
            Member confirmed = Member.builder()
                    .memberId(MEMBER_ID)
                    .email(VALID_EMAIL)
                    .firstName("John")
                    .lastName("Doe")
                    .status(MemberStatus.CONFIRMED)
                    .build();

            AccountActivatedRequest request = AccountActivatedRequest.newBuilder()
                    .setMemberId(MEMBER_ID.toString())
                    .setKeycloakUserId("kc-user-123")
                    .build();

            when(memberRepository.getByMemberId(MEMBER_ID)).thenReturn(Optional.of(confirmed));

            service.notifyAccountActivated(request, activatedObserver);

            verify(activatedObserver).onNext(activatedCaptor.capture());
            verify(activatedObserver).onCompleted();
            assertThat(activatedCaptor.getValue().getSuccess()).isTrue();
            verify(memberRepository).save(any(Member.class));
            verify(eventPublisher).publishEvent(any(MemberActivatedEvent.class));
        }

        @Test
        @DisplayName("should fail for non-existent member")
        void shouldFailForNonExistentMember() {
            AccountActivatedRequest request = AccountActivatedRequest.newBuilder()
                    .setMemberId(MEMBER_ID.toString())
                    .setKeycloakUserId("kc-user-123")
                    .build();

            when(memberRepository.getByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

            service.notifyAccountActivated(request, activatedObserver);

            verify(activatedObserver).onNext(activatedCaptor.capture());
            assertThat(activatedCaptor.getValue().getSuccess()).isFalse();
            assertThat(activatedCaptor.getValue().getMessage()).contains("not found");
        }

        @Test
        @DisplayName("should fail for member not in CONFIRMED status")
        void shouldFailForNonConfirmedMember() {
            Member pending = Member.builder()
                    .memberId(MEMBER_ID)
                    .email(VALID_EMAIL)
                    .firstName("Jane")
                    .status(MemberStatus.PENDING)
                    .build();

            AccountActivatedRequest request = AccountActivatedRequest.newBuilder()
                    .setMemberId(MEMBER_ID.toString())
                    .setKeycloakUserId("kc-user-123")
                    .build();

            when(memberRepository.getByMemberId(MEMBER_ID)).thenReturn(Optional.of(pending));

            service.notifyAccountActivated(request, activatedObserver);

            verify(activatedObserver).onNext(activatedCaptor.capture());
            assertThat(activatedCaptor.getValue().getSuccess()).isFalse();
            assertThat(activatedCaptor.getValue().getMessage()).contains("not in CONFIRMED status");
        }

        @Test
        @DisplayName("should reject empty memberId with INVALID_ARGUMENT")
        void shouldRejectEmptyMemberId() {
            AccountActivatedRequest request = AccountActivatedRequest.newBuilder()
                    .setMemberId("")
                    .setKeycloakUserId("kc-user-123")
                    .build();

            service.notifyAccountActivated(request, activatedObserver);

            verify(activatedObserver).onError(any(StatusRuntimeException.class));
        }

        @Test
        @DisplayName("should reject empty keycloakUserId with INVALID_ARGUMENT")
        void shouldRejectEmptyKeycloakUserId() {
            AccountActivatedRequest request = AccountActivatedRequest.newBuilder()
                    .setMemberId(MEMBER_ID.toString())
                    .setKeycloakUserId("")
                    .build();

            service.notifyAccountActivated(request, activatedObserver);

            verify(activatedObserver).onError(any(StatusRuntimeException.class));
        }

        @Test
        @DisplayName("should return INTERNAL on unexpected exception")
        void shouldReturnInternalOnException() {
            AccountActivatedRequest request = AccountActivatedRequest.newBuilder()
                    .setMemberId(MEMBER_ID.toString())
                    .setKeycloakUserId("kc-user-123")
                    .build();

            when(memberRepository.getByMemberId(MEMBER_ID)).thenThrow(new RuntimeException("DB error"));

            service.notifyAccountActivated(request, activatedObserver);

            verify(activatedObserver).onError(any(StatusRuntimeException.class));
        }
    }

    // ========================================================================
    // Tests - notifyLocalCredentialsAdded
    // ========================================================================

    @Nested
    @DisplayName("notifyLocalCredentialsAdded")
    class NotifyLocalCredentialsAdded {

        @Mock
        private StreamObserver<LocalCredentialsAddedResponse> credentialsObserver;

        @Captor
        private ArgumentCaptor<LocalCredentialsAddedResponse> credentialsCaptor;

        @Test
        @DisplayName("should update hasLocalCredentials flag successfully")
        void shouldUpdateFlagSuccessfully() {
            Member member = Member.builder()
                    .memberId(MEMBER_ID)
                    .email(VALID_EMAIL)
                    .firstName("John")
                    .keycloakUserId("kc-user-123")
                    .hasLocalCredentials(false)
                    .status(MemberStatus.ACTIVE)
                    .build();

            LocalCredentialsAddedRequest request = LocalCredentialsAddedRequest.newBuilder()
                    .setKeycloakUserId("kc-user-123")
                    .build();

            when(memberRepository.getByKeycloakUserId("kc-user-123")).thenReturn(Optional.of(member));

            service.notifyLocalCredentialsAdded(request, credentialsObserver);

            verify(credentialsObserver).onNext(credentialsCaptor.capture());
            verify(credentialsObserver).onCompleted();
            assertThat(credentialsCaptor.getValue().getSuccess()).isTrue();
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("should return failure for unknown keycloakUserId")
        void shouldReturnFailureForUnknownUser() {
            LocalCredentialsAddedRequest request = LocalCredentialsAddedRequest.newBuilder()
                    .setKeycloakUserId("unknown-kc-id")
                    .build();

            when(memberRepository.getByKeycloakUserId("unknown-kc-id")).thenReturn(Optional.empty());

            service.notifyLocalCredentialsAdded(request, credentialsObserver);

            verify(credentialsObserver).onNext(credentialsCaptor.capture());
            assertThat(credentialsCaptor.getValue().getSuccess()).isFalse();
            assertThat(credentialsCaptor.getValue().getMessage()).contains("not found");
        }

        @Test
        @DisplayName("should reject empty keycloakUserId with INVALID_ARGUMENT")
        void shouldRejectEmptyKeycloakUserId() {
            LocalCredentialsAddedRequest request = LocalCredentialsAddedRequest.newBuilder()
                    .setKeycloakUserId("")
                    .build();

            service.notifyLocalCredentialsAdded(request, credentialsObserver);

            verify(credentialsObserver).onError(any(StatusRuntimeException.class));
        }

        @Test
        @DisplayName("should return INTERNAL on unexpected exception")
        void shouldReturnInternalOnException() {
            LocalCredentialsAddedRequest request = LocalCredentialsAddedRequest.newBuilder()
                    .setKeycloakUserId("kc-user-123")
                    .build();

            when(memberRepository.getByKeycloakUserId("kc-user-123")).thenThrow(new RuntimeException("DB error"));

            service.notifyLocalCredentialsAdded(request, credentialsObserver);

            verify(credentialsObserver).onError(any(StatusRuntimeException.class));
        }
    }
}
