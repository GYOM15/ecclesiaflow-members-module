package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import com.ecclesiaflow.io.persistence.mappers.MemberPersistenceMapper;
import com.ecclesiaflow.io.persistence.jpa.SpringDataMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du repository de domaine pour les membres EcclesiaFlow.
 * <p>
 * Cette classe fait le pont entre la couche domaine et la couche de persistance JPA.
 * Elle utilise le pattern Repository avec adaptation entre les entités JPA et les
 * objets de domaine, respectant ainsi la séparation des couches architecturales.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Repository - Adaptation domaine/persistance</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Adaptation entre objets domaine {@link Member} et entités JPA {@link MemberEntity}</li>
 *   <li>Délégation des opérations CRUD vers Spring Data JPA</li>
 *   <li>Conversion bidirectionnelle via {@link MemberPersistenceMapper}</li>
 *   <li>Encapsulation de la logique de persistance</li>
 * </ul>
 * 
 * <p><strong>Pattern d'adaptation :</strong></p>
 * <ul>
 *   <li>Entrée : Objets domaine (Member)</li>
 *   <li>Conversion : Mapper vers entités JPA (MemberEntity)</li>
 *   <li>Persistance : Délégation vers SpringDataMemberRepository</li>
 *   <li>Sortie : Conversion retour vers objets domaine</li>
 * </ul>
 * 
 * <p><strong>Avantages architecturaux :</strong></p>
 * <ul>
 *   <li>Isolation du domaine métier des détails de persistance</li>
 *   <li>Flexibilité pour changer la technologie de persistance</li>
 *   <li>Testabilité avec mocks du repository domaine</li>
 *   <li>Respect des principes DDD (Domain-Driven Design)</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (bean Spring), gestion transactionnelle déléguée,
 * conversion fidèle des données, encapsulation de la persistance.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberRepository
 * @see SpringDataMemberRepository
 * @see MemberPersistenceMapper
 */
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final SpringDataMemberRepository springDataRepo;
    private final MemberPersistenceMapper mapper;

    @Override
    public Optional<Member> findById(UUID id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepo.existsByEmail(email);
    }

    @Override
    public List<Member> findByConfirmedStatus(boolean confirmed) {
        return springDataRepo.findByConfirmed(confirmed).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countConfirmedMembers() {
        return springDataRepo.countByConfirmedTrue();
    }

    @Override
    public long countPendingConfirmations() {
        return springDataRepo.countByConfirmedFalse();
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = mapper.toEntity(member);
        MemberEntity savedEntity = springDataRepo.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Member member) {
        springDataRepo.delete(mapper.toEntity(member));
    }

    @Override
    public List<Member> findAll() {
        return springDataRepo.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
