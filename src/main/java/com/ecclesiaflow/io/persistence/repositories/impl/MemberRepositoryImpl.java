package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.services.repositories.MemberRepository;
import com.ecclesiaflow.io.persistence.entities.MemberEntity;
import com.ecclesiaflow.io.persistence.mappers.MemberPersistenceMapper;
import com.ecclesiaflow.io.persistence.repositories.SpringDataMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
