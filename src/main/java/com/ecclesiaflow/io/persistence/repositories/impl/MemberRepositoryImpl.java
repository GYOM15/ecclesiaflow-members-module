package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import com.ecclesiaflow.io.persistence.mappers.MemberPersistenceMapper;
import com.ecclesiaflow.io.persistence.jpa.SpringDataMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapts the domain {@link MemberRepository} port to Spring Data JPA,
 * converting between {@link Member} domain objects and {@link MemberEntity} JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final SpringDataMemberRepository springDataRepo;
    private final MemberPersistenceMapper mapper;

    @Override
    public Optional<Member> getByMemberId(UUID memberId) {
        return springDataRepo.findByMemberId(memberId).map(mapper::toDomain);
    }

    @Override
    public Optional<Member> getByEmail(String email) {
        return springDataRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Member> getByKeycloakUserId(String keycloakUserId) {
        return springDataRepo.findByKeycloakUserId(keycloakUserId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepo.existsByEmail(email);
    }

    @Override
    public boolean existsByKeycloakUserId(String keycloakUserId) {
        return springDataRepo.existsByKeycloakUserId(keycloakUserId);
    }

    @Override
    public long countByStatus(MemberStatus status) {
        return springDataRepo.countByStatus(status);
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

    @Override
    public Page<Member> getAll(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return springDataRepo.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Member> getByStatus(MemberStatus status, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return springDataRepo.findByStatus(status, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Member> getMembersBySearchTerm(String searchTerm, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return springDataRepo.findMembersBySearchTerm(searchTerm, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Member> getMembersBySearchTermAndStatus(
            String searchTerm, MemberStatus status, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return springDataRepo.findMembersBySearchTermAndStatus(
                searchTerm, status, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Member> findDeactivatedBefore(LocalDateTime cutoffDate) {
        return springDataRepo.findByStatusAndDeactivatedAtBefore(MemberStatus.DEACTIVATED, cutoffDate)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
