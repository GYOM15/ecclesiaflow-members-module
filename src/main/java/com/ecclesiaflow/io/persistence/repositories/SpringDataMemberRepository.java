package com.ecclesiaflow.io.persistence.repositories;

import com.ecclesiaflow.io.persistence.entities.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMemberRepository extends JpaRepository<MemberEntity, UUID> {
    Optional<MemberEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<MemberEntity> findByConfirmed(boolean confirmed);

    long countByConfirmedTrue();

    long countByConfirmedFalse();
}
