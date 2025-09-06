package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.MemberConfirmation;
import com.ecclesiaflow.io.persistence.entities.MemberConfirmationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la conversion entre les objets du domaine MemberConfirmation 
 * et les entités de persistance MemberConfirmationEntity.
 * <p>
 * Cette classe assure la transformation bidirectionnelle entre la couche domaine
 * et la couche persistance pour les confirmations de membres. Elle isole
 * complètement le domaine métier des détails de persistance JPA.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Mapper de persistance - Pont domaine/persistance</p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Conversion MemberConfirmationEntity → MemberConfirmation (domaine)</li>
 *   <li>Conversion MemberConfirmation → MemberConfirmationEntity (persistance)</li>
 *   <li>Isolation des couches domaine et persistance</li>
 *   <li>Préservation de l'intégrité des données lors des transformations</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, stateless, transformations pures.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
public class MemberConfirmationPersistenceMapper {

    /**
     * Convertit une entité de persistance MemberConfirmationEntity en un objet du domaine MemberConfirmation.
     * <p>
     * Cette méthode transforme une entité JPA chargée depuis la base de données
     * en un objet métier immutable du domaine, prêt à être utilisé par les services.
     * </p>
     *
     * @param entity l'entité de persistance, non null
     * @return l'objet du domaine correspondant
     * @throws IllegalArgumentException si entity est null
     */
    public MemberConfirmation toDomain(MemberConfirmationEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne peut pas être null");
        }

        return MemberConfirmation.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .code(entity.getCode())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    /**
     * Convertit un objet du domaine MemberConfirmation en une entité de persistance MemberConfirmationEntity.
     * <p>
     * Cette méthode transforme un objet métier du domaine en une entité JPA
     * prête à être persistée en base de données. Les champs gérés automatiquement
     * par JPA (comme createdAt avec @CreationTimestamp) sont préservés s'ils existent.
     * </p>
     *
     * @param domain l'objet du domaine, non null
     * @return l'entité de persistance correspondante
     * @throws IllegalArgumentException si domain est null
     */
    public MemberConfirmationEntity toEntity(MemberConfirmation domain) {
        if (domain == null) {
            throw new IllegalArgumentException("L'objet domaine ne peut pas être null");
        }

        return MemberConfirmationEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .code(domain.getCode())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getExpiresAt())
                .build();
    }
}
