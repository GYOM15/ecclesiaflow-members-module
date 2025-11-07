package com.ecclesiaflow.io.grpc.server;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.grpc.members.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Réception des appels gRPC depuis d'autres modules (notamment Auth)</li>
 *   <li>Validation des requêtes gRPC (format email)</li>
 *   <li>Conversion messages Protobuf ↔ objets métier Java</li>
 *   <li>Délégation aux services métier existants ({@link MemberService})</li>
 *   <li>Gestion d'erreurs et conversion vers Status gRPC appropriés</li>
 * </ul>
 *
 * <p><strong>Clean Architecture :</strong></p>
 * <pre>
 * gRPC Request → MembersGrpcServiceImpl (Adapter) → MemberService (Business)
 * </pre>
 *
 * <p><strong>Gestion d'erreurs :</strong></p>
 * <ul>
 *   <li>INVALID_ARGUMENT - Données invalides (email format)</li>
 *   <li>NOT_FOUND - Membre n'existe pas</li>
 *   <li>INTERNAL - Erreur interne lors du traitement</li>
 * </ul>
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

    /**
     * Récupère le statut de confirmation d'un membre par son email.
     * <p>
     * Cette méthode est appelée par le module Auth pour vérifier si un membre
     * a confirmé son email avant d'autoriser la connexion.
     * </p>
     *
     * <p><strong>Flow de communication :</strong></p>
     * <pre>
     * 1. Auth: Tentative de connexion d'un utilisateur
     * 2. Auth → Members (gRPC): GetMemberConfirmationStatus(email)
     * 3. Members: Recherche membre en DB
     * 4. Members → Auth (gRPC): ConfirmationStatusResponse
     * 5. Auth: Autorise ou bloque connexion
     * </pre>
     *
     * @param request contient l'email du membre à vérifier
     * @param responseObserver observer pour envoyer la réponse asynchrone
     */
    @Override
    public void getMemberConfirmationStatus(
            ConfirmationStatusRequest request,
            StreamObserver<ConfirmationStatusResponse> responseObserver) {

        String email = request.getEmail();

        try {
            // Validation de l'email
            validateEmail(email);

            // Recherche du membre par email
            Optional<Member> memberOpt = memberRepository.getByEmail(email);

            if (memberOpt.isEmpty()) {
                // Membre n'existe pas
                ConfirmationStatusResponse response = ConfirmationStatusResponse.newBuilder()
                        .setMemberExists(false)
                        .setIsConfirmed(false)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Membre existe, extraire les informations
            Member member = memberOpt.get();
            boolean isConfirmed = member.getConfirmedAt() != null;

            ConfirmationStatusResponse response = ConfirmationStatusResponse.newBuilder()
                    .setMemberExists(true)
                    .setIsConfirmed(isConfirmed)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("An unexpected error occurred")
                    .asRuntimeException());
        }
    }

    // ========================================================================
    // Méthodes utilitaires privées
    // ========================================================================

    /**
     * Valide le format d'une adresse email.
     *
     * @param email l'email à valider
     * @throws IllegalArgumentException si l'email est invalide
     */
    private void validateEmail(String email) {
        // Protobuf ne retourne jamais null, mais une chaîne vide par défaut
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Validation basique du format email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
