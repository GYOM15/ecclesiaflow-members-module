package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.business.services.repositories.MemberConfirmationRepository;
import com.ecclesiaflow.io.persistence.entities.MemberConfirmationEntity;
import com.ecclesiaflow.io.persistence.mappers.MemberConfirmationPersistenceMapper;
import com.ecclesiaflow.io.persistence.repositories.SpringDataMemberConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation concrète du repository pour les confirmations de membres.
 * <p>
 * Cette classe fait le pont entre le domaine métier (MemberConfirmation) et la couche
 * de persistance (MemberConfirmationEntity). Elle utilise le mapper de persistance
 * pour convertir les objets entre les deux couches et orchestre les appels
 * vers le repository Spring Data JPA.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Implémentation repository - Pont domaine/persistance</p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Implémentation du contrat MemberConfirmationRepository</li>
 *   <li>Orchestration des appels vers Spring Data JPA</li>
 *   <li>Conversion bidirectionnelle via le mapper de persistance</li>
 *   <li>Gestion des transactions pour les opérations complexes</li>
 * </ul>
 * 
 * <p><strong>Pattern utilisé :</strong> Repository Pattern avec Adapter Pattern</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, isolation des couches.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class MemberConfirmationRepositoryImpl implements MemberConfirmationRepository {

    private final SpringDataMemberConfirmationRepository springDataRepo;
    private final MemberConfirmationPersistenceMapper mapper;

    @Override
    public Optional<MemberConfirmation> findByMemberId(UUID memberId) {
        return springDataRepo.findByMemberId(memberId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<MemberConfirmation> findByMemberIdAndCode(UUID memberId, String code) {
        return springDataRepo.findByMemberIdAndCode(memberId, code)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<MemberConfirmation> findByCode(String code) {
        return springDataRepo.findByCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByMemberId(UUID memberId) {
        return springDataRepo.existsByMemberId(memberId);
    }

    @Override
    public List<MemberConfirmation> findExpiredConfirmations() {
        return springDataRepo.findExpiredConfirmations(LocalDateTime.now())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countPendingConfirmations() {
        return springDataRepo.countPendingConfirmations(LocalDateTime.now());
    }

    @Override
    public MemberConfirmation save(MemberConfirmation confirmation) {
        MemberConfirmationEntity entity = mapper.toEntity(confirmation);
        MemberConfirmationEntity savedEntity = springDataRepo.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void delete(MemberConfirmation confirmation) {
        springDataRepo.delete(mapper.toEntity(confirmation));
    }

    @Override
    @Transactional
    public int deleteExpiredConfirmations() {
        return springDataRepo.deleteExpiredConfirmations(LocalDateTime.now());
    }
}
