package com.ecclesiaflow.business.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for member persistence.
 * Implementations handle the mapping to/from the actual storage technology.
 */
public interface MemberRepository {

    Optional<Member> getByMemberId(UUID memberId);

    Optional<Member> getByEmail(String email);

    Optional<Member> getByKeycloakUserId(String keycloakUserId);

    boolean existsByEmail(String email);

    boolean existsByKeycloakUserId(String keycloakUserId);

    List<Member> findAll();

    long countByStatus(MemberStatus status);

    Member save(Member member);

    void delete(Member member);

    Page<Member> getAll(Pageable pageable);

    Page<Member> getByStatus(MemberStatus status, Pageable pageable);

    /**
     * Searches members by first name, last name, or email (case-insensitive LIKE).
     */
    Page<Member> getMembersBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Searches members by first name, last name, or email AND status (case-insensitive LIKE).
     */
    Page<Member> getMembersBySearchTermAndStatus(
        String searchTerm, MemberStatus status, Pageable pageable);
}
