package com.ecclesiaflow.io.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPendingEmailChangeRepository extends JpaRepository<PendingEmailChangeEntity, UUID> {

    Optional<PendingEmailChangeEntity> findByToken(UUID token);

    Optional<PendingEmailChangeEntity> findByMemberId(UUID memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM PendingEmailChangeEntity p WHERE p.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") UUID memberId);
}
