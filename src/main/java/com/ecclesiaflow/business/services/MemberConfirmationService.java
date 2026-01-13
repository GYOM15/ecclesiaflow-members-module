package com.ecclesiaflow.business.services;

import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.io.exception.ConfirmationEmailException;

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
 *   <li>Validation des tokens de confirmation reçus par email</li>
 *   <li>Génération de nouveaux tokens de confirmation sécurisés (UUID)</li>
 *   <li>Gestion de l'expiration des tokens de confirmation</li>
 *   <li>Mise à jour du statut de confirmation des membres</li>
 *   <li>Orchestration avec le service d'email pour l'envoi des liens</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberRepository} - Accès aux données membres</li>
 *   <li>{@link MemberConfirmationRepository} - Gestion des codes</li>
 *   <li>{@link EmailClient} - Envoi des emails de confirmation</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Confirmation d'un compte après inscription via lien email</li>
 *   <li>Renvoi d'un token de confirmation expiré</li>
 *   <li>Validation des tokens de confirmation</li>
 *   <li>Gestion des tokens expirés ou invalides</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, sécurisé (tokens à usage unique).</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface MemberConfirmationService {
    
    /**
     * Confirme un membre en validant son token de confirmation.
     * <p>
     * Cette méthode valide le token de confirmation reçu par email,
     * vérifie sa validité et son expiration, puis met à jour le statut
     * du membre à "confirmé". Génère un token temporaire pour la définition
     * du mot de passe.
     * </p>
     * 
     * @param token le token de confirmation UUID reçu par email, non null
     * @return le résultat de la confirmation avec token temporaire et statut
     * @throws MemberNotFoundException si le membre associé au token n'existe pas
     * @throws InvalidConfirmationCodeException si le token est invalide ou expiré
     * @throws MemberAlreadyConfirmedException si le membre est déjà confirmé
     * @throws IllegalArgumentException si token est null
     * 
     * @implNote Opération transactionnelle avec mise à jour du statut et suppression du token.
     */
    MembershipConfirmationResult confirmMemberByToken(UUID token);

    /**
     * Génère et envoie un nouveau lien de confirmation pour un membre.
     * <p>
     * Cette méthode génère un nouveau token de confirmation sécurisé (UUID),
     * l'associe au membre spécifié, et orchestre l'envoi de l'email
     * contenant le lien de confirmation. Invalide les tokens précédents.
     * </p>
     *
     * @param member le membre pour lequel générer un token, non null
     * @throws MemberNotFoundException si le membre n'existe pas
     * @throws MemberAlreadyConfirmedException si le membre est déjà confirmé
     * @throws ConfirmationEmailException si l'envoi de l'email échoue
     * @throws IllegalArgumentException si member est null
     *
     * @implNote Opération transactionnelle avec génération UUID cryptographiquement sécurisée.
     */
    void sendConfirmationLink(Member member);

    /**
     * Génère et envoie un nouveau lien de confirmation pour un membre.
     * <p>
     * Cette méthode recherche le membre par son email, génère un nouveau token 
     * de confirmation sécurisé (UUID), l'associe au membre, et orchestre l'envoi 
     * de l'email contenant le lien de confirmation. Invalide les tokens précédents.
     * </p>
     * 
     * <p><strong>Sécurité anti-énumération (approche hybride):</strong>
     * <ul>
     *   <li><strong>Email inexistant:</strong> Silencieux (pas d'exception) pour éviter l'énumération</li>
     *   <li><strong>Compte déjà confirmé:</strong> Lève une exception pour une UX claire</li>
     * </ul>
     * </p>
     *
     * @param email l'adresse email du membre pour lequel générer un token, non null
     * @throws MemberAlreadyConfirmedException si le membre est déjà confirmé
     * @throws ConfirmationEmailException si l'envoi de l'email échoue
     * @throws IllegalArgumentException si email est null ou invalide
     *
     * @implNote Opération transactionnelle avec génération UUID cryptographiquement sécurisée.
     *           Ne lève pas MemberNotFoundException si l'email n'existe pas (anti-énumération).
     */
    void sendConfirmationLink(String email);
}
