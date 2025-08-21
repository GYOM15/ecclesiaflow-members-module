package com.ecclesiaflow.io.repository;

import com.ecclesiaflow.io.entities.MemberConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberConfirmationRepository extends JpaRepository<MemberConfirmation, UUID> {

    Optional<MemberConfirmation> findByMemberId(UUID memberId);

    Optional<MemberConfirmation> findByMemberIdAndCode(UUID memberId, String code);

    Optional<MemberConfirmation> findByCode(String code);

    @Modifying
    @Query("DELETE FROM MemberConfirmation mc WHERE mc.expiresAt < :now")
    int deleteExpiredConfirmations(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(mc) FROM MemberConfirmation mc WHERE mc.expiresAt > :now")
    long countPendingConfirmations(@Param("now") LocalDateTime now);

    boolean existsByMemberId(UUID memberId);
}
