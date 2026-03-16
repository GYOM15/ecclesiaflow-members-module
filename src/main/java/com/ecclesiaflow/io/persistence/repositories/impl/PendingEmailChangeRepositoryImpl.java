package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.emailchange.PendingEmailChange;
import com.ecclesiaflow.business.domain.emailchange.PendingEmailChangeRepository;
import com.ecclesiaflow.io.persistence.jpa.PendingEmailChangeEntity;
import com.ecclesiaflow.io.persistence.jpa.SpringDataPendingEmailChangeRepository;
import com.ecclesiaflow.io.persistence.mappers.PendingEmailChangePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PendingEmailChangeRepositoryImpl implements PendingEmailChangeRepository {

    private final SpringDataPendingEmailChangeRepository springDataRepo;
    private final PendingEmailChangePersistenceMapper mapper;

    @Override
    public PendingEmailChange save(PendingEmailChange pendingChange) {
        PendingEmailChangeEntity entity = mapper.toEntity(pendingChange);
        PendingEmailChangeEntity saved = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PendingEmailChange> getByToken(UUID token) {
        return springDataRepo.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<PendingEmailChange> getByMemberId(UUID memberId) {
        return springDataRepo.findByMemberId(memberId).map(mapper::toDomain);
    }

    @Override
    public void delete(PendingEmailChange pendingChange) {
        springDataRepo.delete(mapper.toEntity(pendingChange));
    }

    @Override
    @Transactional
    public void deleteByMemberId(UUID memberId) {
        springDataRepo.deleteByMemberId(memberId);
    }
}
