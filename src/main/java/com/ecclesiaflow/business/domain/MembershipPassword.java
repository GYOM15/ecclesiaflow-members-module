package com.ecclesiaflow.business.domain;

import com.ecclesiaflow.business.mappers.web.PasswordSetMapper;
import lombok.Builder;
import lombok.Data;

/**
 * Objet métier représentant les données de définition de mot de passe EcclesiaFlow.
 * <p>
 * Cette classe encapsule les informations nécessaires pour la définition
 * du mot de passe initial d'un membre : email d'identification et nouveau
 * mot de passe. Fait partie de la couche métier et isole les données
 * de définition de mot de passe des détails de transport (DTOs) et de
 * persistance (entités).
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Objet métier - Encapsulation des données de mot de passe</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Encapsulation des données email + mot de passe pour la couche métier</li>
 *   <li>Isolation des détails de transport et de persistance</li>
 *   <li>Validation implicite des données via les annotations Lombok</li>
 *   <li>Support du pattern Builder pour une construction d'objet claire</li>
 * </ul>
 * 
 * <p><strong>Cycle de vie typique :</strong></p>
 * <ol>
 *   <li>Création via {@link PasswordSetMapper} depuis une requête web</li>
 *   <li>Traitement par {@link MemberPasswordService} pour validation métier</li>
 *   <li>Transmission au module d'authentification pour persistance</li>
 * </ol>
 * 
 * <p><strong>Patterns utilisés :</strong></p>
 * <ul>
 *   <li>Builder Pattern - Construction d'objet fluide et lisible</li>
 *   <li>Value Object Pattern - Objet immutable représentant une valeur métier</li>
 *   <li>Data Transfer Object - Transport de données entre couches</li>
 * </ul>
 * 
 * <p><strong>Sécurité :</strong></p>
 * <ul>
 *   <li>Le mot de passe est stocké temporairement en mémoire uniquement</li>
 *   <li>Aucune persistance directe de cet objet en base de données</li>
 *   <li>Transmission sécurisée vers le module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (immutable), construction via Builder,
 * séparation des responsabilités, encapsulation des données métier.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see PasswordSetMapper
 * @see MemberPasswordService
 */
@Data
@Builder
public class MembershipPassword {
    
    /**
     * Adresse email du membre pour lequel définir le mot de passe.
     * <p>
     * Utilisé comme identifiant unique pour localiser le membre
     * dans le système et valider la cohérence avec l'ID fourni
     * dans l'URL de la requête.
     * </p>
     */
    private String email;
    
    /**
     * Nouveau mot de passe à définir pour le membre.
     * <p>
     * Mot de passe en clair qui sera transmis au module d'authentification
     * pour hachage et stockage sécurisé. N'est jamais persisté en clair
     * dans ce module.
     * </p>
     */
    private String password;
}
