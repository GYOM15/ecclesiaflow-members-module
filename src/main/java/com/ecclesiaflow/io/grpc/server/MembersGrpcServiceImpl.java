package com.ecclesiaflow.io.grpc.server;

import com.ecclesiaflow.business.domain.events.MemberActivatedEvent;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.grpc.members.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service gRPC de gestion des membres.
 * <p>
 * Cette classe implémente le service {@code ecclesiaflow.members.MembersService} défini
 * dans le fichier proto. Elle sert de pont (adapter) entre le protocole gRPC et
 * les services métier existants du module Members.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Adapter - gRPC to Business Logic</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembersServiceGrpc.MembersServiceImplBase
 * @see MemberRepository
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class MembersGrpcServiceImpl extends MembersServiceGrpc.MembersServiceImplBase {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Récupère le statut de confirmation d'un membre par son email.
     *
     * @param request          contient l'email du membre à vérifier
     * @param responseObserver observer pour envoyer la réponse asynchrone
     */
    @Override
    public void getMemberConfirmationStatus(
            ConfirmationStatusRequest request,
            StreamObserver<ConfirmationStatusResponse> responseObserver) {

        try {
            String email = request.getEmail();
            validateEmail(email);

            ConfirmationStatusResponse response = buildConfirmationStatusResponse(email);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            handleInvalidArgument(responseObserver, e);
        } catch (Exception e) {
            handleInternalError(responseObserver);
        }
    }

    /**
     * Handles notification from Auth module that a member account has been activated.
     *
     * @param request          contains memberId and keycloakUserId
     * @param responseObserver observer for async response
     */
    @Override
    @Transactional
    public void notifyAccountActivated(
            AccountActivatedRequest request,
            StreamObserver<AccountActivatedResponse> responseObserver) {

        try {
            validateActivationRequest(request);

            UUID memberId = UUID.fromString(request.getMemberId());
            String keycloakUserId = request.getKeycloakUserId();

            AccountActivatedResponse response = processAccountActivation(memberId, keycloakUserId);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            handleInvalidArgument(responseObserver, e);
        } catch (Exception e) {
            handleActivationError(responseObserver);
        }
    }

    // ========================================================================
    // Private helper methods - Confirmation Status
    // ========================================================================

    private ConfirmationStatusResponse buildConfirmationStatusResponse(String email) {
        Optional<Member> memberOpt = memberRepository.getByEmail(email);

        return memberOpt.map(this::buildMemberExistsResponse)
                .orElseGet(this::buildMemberNotFoundResponse);

    }

    private ConfirmationStatusResponse buildMemberNotFoundResponse() {
        return ConfirmationStatusResponse.newBuilder()
                .setMemberExists(false)
                .setIsConfirmed(false)
                .build();
    }

    private ConfirmationStatusResponse buildMemberExistsResponse(Member member) {
        return ConfirmationStatusResponse.newBuilder()
                .setMemberExists(true)
                .setIsConfirmed(member.isConfirmed())
                .build();
    }

    // ========================================================================
    // Private helper methods - Account Activation
    // ========================================================================

    private void validateActivationRequest(AccountActivatedRequest request) {
        if (request.getMemberId().isBlank()) {
            throw new IllegalArgumentException("member_id cannot be empty");
        }
        if (request.getKeycloakUserId().isBlank()) {
            throw new IllegalArgumentException("keycloak_user_id cannot be empty");
        }
    }

    private AccountActivatedResponse processAccountActivation(UUID memberId, String keycloakUserId) {
        Optional<Member> memberOpt = memberRepository.getByMemberId(memberId);

        if (memberOpt.isEmpty()) {
            return buildActivationFailureResponse("Member not found: " + memberId);
        }

        Member member = memberOpt.get();

        if (!isMemberConfirmed(member)) {
            return buildActivationFailureResponse(
                    "Member is not in CONFIRMED status: " + member.getStatus()
            );
        }

        activateMember(member, keycloakUserId);
        return buildActivationSuccessResponse();
    }

    private boolean isMemberConfirmed(Member member) {
        return member.getStatus() == MemberStatus.CONFIRMED;
    }

    private void activateMember(Member member, String keycloakUserId) {
        Member updatedMember = member.toBuilder()
                .status(MemberStatus.ACTIVE)
                .keycloakUserId(keycloakUserId)
                .build();

        memberRepository.save(updatedMember);

        eventPublisher.publishEvent(
                new MemberActivatedEvent(updatedMember.getEmail(), updatedMember.getFirstName())
        );
    }

    private AccountActivatedResponse buildActivationSuccessResponse() {
        return AccountActivatedResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Member activated successfully")
                .build();
    }

    private AccountActivatedResponse buildActivationFailureResponse(String message) {
        return AccountActivatedResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .build();
    }

    // ========================================================================
    // Validation methods
    // ========================================================================

    private void validateEmail(String email) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // ========================================================================
    // Error handling methods
    // ========================================================================

    private void handleInvalidArgument(StreamObserver<?> responseObserver, IllegalArgumentException e) {
        responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .withCause(e)
                .asRuntimeException());
    }

    private void handleInternalError(StreamObserver<?> responseObserver) {
        responseObserver.onError(Status.INTERNAL
                .withDescription("An unexpected error occurred")
                .asRuntimeException());
    }

    private void handleActivationError(StreamObserver<?> responseObserver) {
        responseObserver.onError(Status.INTERNAL
                .withDescription("Failed to activate member account")
                .asRuntimeException());
    }
}