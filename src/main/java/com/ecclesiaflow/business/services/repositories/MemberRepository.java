package com.ecclesiaflow.business.services.repositories;

import com.ecclesiaflow.business.domain.member.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {

    Optional<Member> findById(UUID id);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Member> findByConfirmedStatus(boolean confirmed);
    List<Member> findAll();
    long countConfirmedMembers();
    long countPendingConfirmations();
    Member save(Member member);
    void delete(Member member);
}
