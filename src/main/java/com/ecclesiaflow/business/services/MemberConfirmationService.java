package com.ecclesiaflow.business.services;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;

import java.util.UUID;

/**
 * Service de domaine pour la gestion du processus de confirmation des membres EcclesiaFlow.
 * <p>
 * Cette interface définit les opérations liées au processus de confirmation
 * des comptes membres : validation des codes, génération et envoi des codes,
 * et gestion du statut de confirmation. Complète le processus d'inscription.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service de domaine - Confirmation des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Validation des codes de confirmation saisis par les membres</li>
 *   <li>Génération de nouveaux codes de confirmation</li>
 *   <li>Gestion de l'expiration des codes de confirmation</li>
 *   <li>Mise à jour du statut de confirmation des membres</li>
 *   <li>Orchestration avec le service d'email pour l'envoi des codes</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link com.ecclesiaflow.business.services.repositories.MemberRepository} - Accès aux données membres</li>
 *   <li>{@link com.ecclesiaflow.business.services.repositories.MemberConfirmationRepository} - Gestion des codes</li>
 *   <li>{@link EmailService} - Envoi des emails de confirmation</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Confirmation d'un compte après inscription</li>
 *   <li>Renvoi d'un code de confirmation expiré</li>
 *   <li>Validation des tentatives de confirmation multiples</li>
 *   <li>Gestion des codes expirés ou invalides</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, sécurisé (codes à usage unique).</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface MemberConfirmationService {
    
    /**
     * Confirme un membre en validant son code de confirmation.
     * <p>
     * Cette méthode valide le code de confirmation fourni par le membre,
     * vérifie sa validité et son expiration, puis met à jour le statut
     * du membre à "confirmé". Génère un token temporaire pour la définition
     * du mot de passe.
     * </p>
     * 
     * @param confirmationRequest les données de confirmation (memberId, code), non null
     * @return le résultat de la confirmation avec token temporaire et statut
     * @throws com.ecclesiaflow.web.exception.MemberNotFoundException si le membre n'existe pas
     * @throws com.ecclesiaflow.web.exception.InvalidConfirmationCodeException si le code est invalide ou expiré
     * @throws com.ecclesiaflow.web.exception.MemberAlreadyConfirmedException si le membre est déjà confirmé
     * @throws IllegalArgumentException si confirmationRequest est null ou invalide
     * 
     * @implNote Opération transactionnelle avec mise à jour du statut et invalidation du code.
     */
    MembershipConfirmationResult confirmMember(MembershipConfirmation confirmationRequest);
    
    /**
     * Génère et envoie un nouveau code de confirmation pour un membre.
     * <p>
     * Cette méthode génère un nouveau code de confirmation à 6 chiffres,
     * l'associe au membre spécifié, et orchestre l'envoi de l'email
     * de confirmation via le service d'email. Invalide les codes précédents.
     * </p>
     * 
     * @param memberId l'identifiant du membre pour lequel générer un code, non null
     * @throws com.ecclesiaflow.web.exception.MemberNotFoundException si le membre n'existe pas
     * @throws com.ecclesiaflow.web.exception.MemberAlreadyConfirmedException si le membre est déjà confirmé
     * @throws com.ecclesiaflow.web.exception.ConfirmationEmailException si l'envoi de l'email échoue
     * @throws IllegalArgumentException si memberId est null
     * 
     * @implNote Opération transactionnelle avec génération aléatoire sécurisée et envoi email.
     */
    void sendConfirmationCode(UUID memberId);
}
