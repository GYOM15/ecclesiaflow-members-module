package com.ecclesiaflow.io.persistence.jpa;

import com.ecclesiaflow.business.domain.member.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataMemberRepositoryTest {

    @Autowired
    private SpringDataMemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private MemberEntity member1;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        member1 = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .address("101 Pine St")
                .status(MemberStatus.ACTIVE)
                .confirmedAt(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();
        entityManager.persistAndFlush(member1);

        MemberEntity member2 = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .address("202 Elm St")
                .status(MemberStatus.PENDING)
                .createdAt(LocalDateTime.now().minusDays(8))
                .updatedAt(LocalDateTime.now().minusDays(8))
                .build();
        entityManager.persistAndFlush(member2);

        MemberEntity member3 = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie.brown@example.com")
                .address("303 Oak Ave")
                .status(MemberStatus.ACTIVE)
                .confirmedAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now().minusDays(7))
                .build();
        entityManager.persistAndFlush(member3);

        entityManager.clear();
    }

    @Test
    void getByEmail_shouldReturnMember() {
        Optional<MemberEntity> found = memberRepository.findByEmail("alice.smith@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void getByEmail_shouldReturnEmptyWhenNotFound() {
        Optional<MemberEntity> found = memberRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        boolean exists = memberRepository.existsByEmail("bob.johnson@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalseWhenNotFound() {
        boolean exists = memberRepository.existsByEmail("unknown@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void findByStatus_shouldReturnOnlyActiveMembersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> activeMembers = memberRepository.findByStatus(MemberStatus.ACTIVE, pageable);

        assertThat(activeMembers.getContent()).hasSize(2);
        assertThat(activeMembers.getTotalElements()).isEqualTo(2);
        assertThat(activeMembers.getTotalPages()).isEqualTo(1);
        assertThat(activeMembers.getContent()).extracting(MemberEntity::getEmail)
                .containsExactlyInAnyOrder("alice.smith@example.com", "charlie.brown@example.com");
    }

    @Test
    void findByStatus_shouldReturnOnlyPendingMembersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> pendingMembers = memberRepository.findByStatus(MemberStatus.PENDING, pageable);

        assertThat(pendingMembers.getContent()).hasSize(1);
        assertThat(pendingMembers.getTotalElements()).isEqualTo(1);
        assertThat(pendingMembers.getTotalPages()).isEqualTo(1);
        assertThat(pendingMembers.getContent().get(0).getEmail()).isEqualTo("bob.johnson@example.com");
    }

    @Test
    void findMembersBySearchTerm_shouldReturnMatchingMembersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTerm("alice", pageable);

        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getTotalElements()).isEqualTo(1);
        assertThat(searchResults.getContent().get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findMembersBySearchTerm_shouldReturnEmptyPageWhenNoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTerm("nonexistent", pageable);

        assertThat(searchResults.getContent()).isEmpty();
        assertThat(searchResults.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findMembersBySearchTermAndStatus_shouldReturnMatchingActiveMembers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTermAndStatus(
                "alice", MemberStatus.ACTIVE, pageable);

        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getTotalElements()).isEqualTo(1);
        assertThat(searchResults.getContent().get(0).getFirstName()).isEqualTo("Alice");
        assertThat(searchResults.getContent().get(0).getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void findMembersBySearchTermAndStatus_shouldReturnMatchingPendingMembers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTermAndStatus(
                "bob", MemberStatus.PENDING, pageable);

        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getTotalElements()).isEqualTo(1);
        assertThat(searchResults.getContent().get(0).getFirstName()).isEqualTo("Bob");
        assertThat(searchResults.getContent().get(0).getStatus()).isEqualTo(MemberStatus.PENDING);
    }

    @Test
    void findMembersBySearchTermAndStatus_shouldReturnEmptyWhenNoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTermAndStatus(
                "alice", MemberStatus.PENDING, pageable);

        assertThat(searchResults.getContent()).isEmpty();
        assertThat(searchResults.getTotalElements()).isEqualTo(0);
    }

    @Test
    void countByStatus_shouldReturnCorrectActiveCount() {
        long count = memberRepository.countByStatus(MemberStatus.ACTIVE);
        assertThat(count).isEqualTo(2); // Alice and Charlie
    }

    @Test
    void countByStatus_shouldReturnCorrectPendingCount() {
        long count = memberRepository.countByStatus(MemberStatus.PENDING);
        assertThat(count).isEqualTo(1); // Bob
    }

    @Test
    void save_shouldPersistNewMember() {
        MemberEntity newMember = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("David")
                .lastName("Clark")
                .email("david.clark@example.com")
                .address("404 Error St")
                .status(MemberStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MemberEntity savedMember = memberRepository.save(newMember);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("david.clark@example.com");
        assertThat(memberRepository.findById(savedMember.getId())).isPresent();
    }

    @Test
    void update_shouldModifyExistingMember() {
        member1.setFirstName("Alicia");
        member1.setStatus(MemberStatus.PENDING);
        member1.setConfirmedAt(null);
        member1.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member1);
        entityManager.flush();
        entityManager.clear();

        Optional<MemberEntity> updatedMember = memberRepository.findById(member1.getId());
        assertThat(updatedMember).isPresent();
        assertThat(updatedMember.get().getFirstName()).isEqualTo("Alicia");
        assertThat(updatedMember.get().getStatus()).isEqualTo(MemberStatus.PENDING);
        assertThat(updatedMember.get().getConfirmedAt()).isNull();
    }

    @Test
    void delete_shouldRemoveMember() {
        UUID memberIdToDelete = member1.getId();
        memberRepository.deleteById(memberIdToDelete);
        entityManager.flush();
        entityManager.clear();

        Optional<MemberEntity> deletedMember = memberRepository.findById(memberIdToDelete);
        assertThat(deletedMember).isEmpty();
        assertThat(memberRepository.findAll()).hasSize(2);
    }
}
