package com.ecclesiaflow.business.mappers.persistence;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.web.dto.SignUpRequest;

/**
 * Mapper statique pour la conversion entre les DTOs web et les objets métier.
 * <p>
 * Cette classe fournit des méthodes utilitaires pour transformer les requêtes HTTP
 * en objets du domaine métier utilisables par les services. Respecte le pattern
 * Static Utility Class avec des méthodes purement fonctionnelles.
 * </p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Conversion des DTOs web vers les objets métier</li>
 *   <li>Séparation claire entre couche web et couche métier</li>
 *   <li>Validation implicite des transformations</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Transformation des requêtes d'inscription en objets métier</li>
 *   <li>Transformation des requêtes de connexion en identifiants</li>
 *   <li>Orchestration par les contrôleurs REST</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, stateless, opérations pures.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public class MemberMapper {
    
    /**
     * Convertit une requête d'inscription web en objet métier MembershipRegistration.
     * <p>
     * Cette méthode effectue une transformation directe des champs sans validation
     * supplémentaire, la validation ayant été effectuée au niveau DTO.
     * </p>
     * 
     * @param req la requête d'inscription provenant de la couche web, non null
     * @return un objet MembershipRegistration contenant les données d'inscription
     * @throws NullPointerException si req est null
     */
    public static MembershipRegistration fromSignUpRequest(SignUpRequest req) {
        return new MembershipRegistration(
            req.getFirstName(),
            req.getLastName(),
            req.getEmail(),
            req.getAddress()
        );
    }
}
