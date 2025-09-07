package com.ecclesiaflow.business.domain.member;


/**
 * Énumération des rôles fonctionnels disponibles pour les membres d’EcclesiaFlow.
 *
 * <p><strong>Concept métier :</strong> Les rôles définissent les niveaux
 * d’autorisation et les responsabilités associés à un membre dans l’application.</p>
 *
 * <p><strong>Rôle architectural :</strong> Valeur du domaine métier.
 * Représente une autorisation conceptuelle et non un détail technique
 * de persistance ou d’infrastructure.</p>
 *
 * <p><strong>Hiérarchie actuelle :</strong></p>
 * <ul>
 *   <li><strong>MEMBER</strong> — Membre standard avec accès aux fonctionnalités de base</li>
 *   <li><strong>ADMIN</strong> — Administrateur avec accès complet à la gestion</li>
 * </ul>
 *
 * <p><strong>Utilisation :</strong></p>
 * <ul>
 *   <li>Assignation par défaut du rôle {@code MEMBER} lors de l’inscription</li>
 *   <li>Élévation manuelle au rôle {@code ADMIN} par un administrateur existant</li>
 *   <li>Contrôle d’accès aux endpoints, opérations et fonctionnalités sensibles</li>
 * </ul>
 *
 * <p><strong>Évolutivité :</strong> Cette énumération peut être enrichie pour répondre
 * à de nouveaux besoins organisationnels (par ex. {@code PASTOR}, {@code MODERATOR}, etc.).</p>
 *
 * <p><strong>Intégration :</strong> Utilisé dans :
 * <ul>
 *   <li>Le domaine membres (profil et permissions)</li>
 *   <li>Le module d’authentification/autorisation (filtrage des accès)</li>
 * </ul>
 * </p>
 *
 * @author EcclesiaFlow
 * @since 1.0.0
 */
public enum Role {
    /**
     * Membre standard de l'église.
     * <p>
     * Rôle par défaut attribué lors de l'inscription. Permet l'accès aux
     * fonctionnalités de base de l'application : consultation du profil,
     * mise à jour des informations personnelles, participation aux activités.
     * </p>
     */
    MEMBER,
    
    /**
     * Administrateur de l'application.
     * <p>
     * Rôle avec privilèges élevés permettant la gestion complète de l'application :
     * gestion des membres, accès aux statistiques, configuration du système,
     * modération du contenu.
     * </p>
     */
    ADMIN
}
