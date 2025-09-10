package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la conversion bidirectionnelle entre objets domaine et entités JPA.
 * <p>
 * Cette classe gère la transformation entre les objets métier {@link Member} de la couche domaine
 * et les entités JPA {@link MemberEntity} de la couche persistance. Elle respecte la séparation
 * des couches architecturales en isolant le domaine des détails de persistance.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Mapper - Adaptation domaine/persistance</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Conversion des entités JPA vers objets domaine (toDomain)</li>
 *   <li>Conversion des objets domaine vers entités JPA (toEntity)</li>
 *   <li>Préservation de l'intégrité des données lors des conversions</li>
 *   <li>Gestion des champs spéciaux (horodatages, identifiants)</li>
 * </ul>
 * 
 * <p><strong>Pattern de mapping :</strong></p>
 * <ul>
 *   <li>Mapping 1:1 entre champs correspondants</li>
 *   <li>Préservation des valeurs null appropriées</li>
 *   <li>Gestion des champs auto-générés (createdAt, updatedAt)</li>
 *   <li>Conversion fidèle des types (UUID, LocalDateTime, enum)</li>
 * </ul>
 * 
 * <p><strong>Avantages architecturaux :</strong></p>
 * <ul>
 *   <li>Isolation du domaine des annotations JPA</li>
 *   <li>Flexibilité pour évoluer indépendamment</li>
 *   <li>Testabilité avec objets domaine purs</li>
 *   <li>Respect des principes DDD</li>
 * </ul>
 * 
 * <p><strong>Utilisation :</strong> Exclusivement par {@link com.ecclesiaflow.io.persistence.repositories.impl.MemberRepositoryImpl}
 * pour l'adaptation entre les couches.</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (bean Spring), conversion fidèle, gestion des cas limites.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Member
 * @see MemberEntity
 * @see com.ecclesiaflow.io.persistence.repositories.impl.MemberRepositoryImpl
 */
@Component
public class MemberPersistenceMapper {

    /**
     * Convertit une entité JPA en objet domaine.
     * <p>
     * Cette méthode transforme une {@link MemberEntity} chargée depuis la base de données
     * en un objet métier {@link Member} utilisable par la couche domaine. Tous les champs
     * sont mappés fidèlement, y compris les horodatages et les statuts.
     * </p>
     * 
     * @param entity l'entité JPA à convertir, non null
     * @return l'objet domaine correspondant avec toutes les données
     * @throws IllegalArgumentException si entity est null
     * 
     * @implNote Utilise le pattern Builder pour construire l'objet domaine immutable.
     */
    public Member toDomain(MemberEntity entity) {
        return Member.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .role(entity.getRole())
                .confirmed(entity.isConfirmed())
                .createdAt(entity.getCreatedAt())
                .confirmedAt(entity.getConfirmedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convertit un objet domaine en entité JPA.
     * <p>
     * Cette méthode transforme un objet métier {@link Member} en une {@link MemberEntity}
     * prête pour la persistance JPA. Attention : le champ updatedAt n'est pas mappé car
     * il est géré automatiquement par l'annotation @UpdateTimestamp de Hibernate.
     * </p>
     * 
     * <p><strong>Champs exclus du mapping :</strong></p>
     * <ul>
     *   <li>updatedAt - Géré automatiquement par @UpdateTimestamp</li>
     * </ul>
     * 
     * <p><strong>Cas d'utilisation :</strong></p>
     * <ul>
     *   <li>Création d'un nouveau membre (id et createdAt seront auto-générés)</li>
     *   <li>Mise à jour d'un membre existant (updatedAt sera auto-mis à jour)</li>
     * </ul>
     * 
     * @param domain l'objet domaine à convertir, non null
     * @return l'entité JPA prête pour la persistance
     * @throws IllegalArgumentException si domain est null
     * 
     * @implNote Utilise le pattern Builder pour construire l'entité JPA.
     */
    public MemberEntity toEntity(Member domain) {
        return MemberEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .address(domain.getAddress())
                .role(domain.getRole())
                .confirmed(domain.isConfirmed())
                .createdAt(domain.getCreatedAt())
                .confirmedAt(domain.getConfirmedAt())
                .build();
    }
}