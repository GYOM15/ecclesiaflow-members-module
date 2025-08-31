package com.ecclesiaflow.io.entities;

/**
 * Énumération des rôles disponibles pour les membres EcclesiaFlow.
 * <p>
 * Cette énumération définit les différents niveaux d'autorisation dans l'application.
 * Utilisée par l'entité {@link Member} pour déterminer les permissions et l'accès
 * aux fonctionnalités selon le rôle attribué.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Énumération - Modèle de données des autorisations</p>
 * 
 * <p><strong>Hiérarchie des rôles :</strong></p>
 * <ul>
 *   <li><strong>MEMBER</strong> - Membre standard avec accès aux fonctionnalités de base</li>
 *   <li><strong>ADMIN</strong> - Administrateur avec accès complet à la gestion</li>
 * </ul>
 * 
 * <p><strong>Utilisation :</strong></p>
 * <ul>
 *   <li>Attribution automatique du rôle MEMBER lors de l'inscription</li>
 *   <li>Élévation manuelle au rôle ADMIN par un administrateur existant</li>
 *   <li>Contrôle d'accès aux endpoints et fonctionnalités</li>
 * </ul>
 * 
 * <p><strong>Intégration :</strong> Cette énumération est utilisée à la fois dans le
 * module membres (pour le profil) et potentiellement dans le module d'authentification
 * (pour les autorisations).</p>
 * 
 * <p><strong>Évolutivité :</strong> Peut être étendue avec de nouveaux rôles selon
 * les besoins futurs (PASTOR, MODERATOR, etc.).</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Member
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
