package com.ecclesiaflow.business.domain.member;

/**
 * Objet métier représentant les données d'inscription d'un nouveau membre EcclesiaFlow.
 * <p>
 * Ce Record encapsule toutes les informations nécessaires pour créer un compte utilisateur
 * dans le système. Utilise le pattern Value Object avec des données immutables.
 * </p>
 *
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Inscription de nouveaux membres via formulaire web</li>
 *   <li>Validation des données avant création de compte</li>
 *   <li>Transfer d'informations entre couches web et métier</li>
 * </ul>
 *
 * <p><strong>Garanties :</strong> Immutable, thread-safe par défaut (Record Java).</p>
 *
 * @param firstName le prénom du membre, non null
 * @param lastName le nom de famille du membre, non null
 * @param email l'adresse email du membre (doit être valide et unique), non null
 * @param address l'adresse du membre, peut être null
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public record MembershipRegistration(
    String firstName,
    String lastName,
    String email,
    String address
) {}
