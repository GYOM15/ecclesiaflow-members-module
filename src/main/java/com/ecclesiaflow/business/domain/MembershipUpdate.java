package com.ecclesiaflow.business.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Objet métier représentant une demande de modification d'un membre.
 * <p>
 * Utilisé dans la couche business pour encapsuler les données de modification
 * d'un membre sans dépendre des DTOs de la couche web.
 * </p>
 * 
 * <p><strong>Architecture :</strong></p>
 * <ul>
 *   <li>DTO web → Mapper → Objet métier → Service</li>
 *   <li>Séparation stricte entre couches web et business</li>
 *   <li>Pas de DTOs dans les services métier</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Data
@Builder
public class MembershipUpdate {
    
    /**
     * ID du membre à modifier
     */
    private UUID memberId;
    
    /**
     * Nouveau prénom (optionnel)
     */
    private String firstName;
    
    /**
     * Nouveau nom de famille (optionnel)
     */
    private String lastName;
    
    /**
     * Nouvelle adresse (optionnel)
     */
    private String address;
    
    /**
     * Nouvel email (optionnel)
     */
    private String email;
}
