package com.ecclesiaflow.io.persistence.jpa;

import com.ecclesiaflow.business.domain.member.Role;
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
    private MemberEntity member2;
    private MemberEntity member3;

    @BeforeEach
    void setUp() {
        // Nettoyage avant chaque test
        memberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Création des membres pour les tests
        member1 = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .address("101 Pine St")
                .role(Role.MEMBER)
                .confirmed(true)
                .confirmedAt(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();
        entityManager.persistAndFlush(member1);

        member2 = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .address("202 Elm St")
                .role(Role.ADMIN)
                .confirmed(false)
                .createdAt(LocalDateTime.now().minusDays(8))
                .updatedAt(LocalDateTime.now().minusDays(8))
                .build();
        entityManager.persistAndFlush(member2);

        member3 = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie.brown@example.com")
                .address("303 Oak Ave")
                .role(Role.MEMBER)
                .confirmed(true)
                .confirmedAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now().minusDays(7))
                .build();
        entityManager.persistAndFlush(member3);

        entityManager.clear(); // Détacher les entités
    }

    @Test
    void findByEmail_shouldReturnMember() {
        Optional<MemberEntity> found = memberRepository.findByEmail("alice.smith@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotFound() {
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
    void findByConfirmed_shouldReturnOnlyConfirmedMembersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> confirmedMembers = memberRepository.findByConfirmed(true, pageable);
        
        assertThat(confirmedMembers.getContent()).hasSize(2);
        assertThat(confirmedMembers.getTotalElements()).isEqualTo(2);
        assertThat(confirmedMembers.getTotalPages()).isEqualTo(1);
        assertThat(confirmedMembers.getContent()).extracting(MemberEntity::getEmail)
                .containsExactlyInAnyOrder("alice.smith@example.com", "charlie.brown@example.com");
    }

    @Test
    void findByConfirmed_shouldReturnOnlyUnconfirmedMembersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> unconfirmedMembers = memberRepository.findByConfirmed(false, pageable);
        
        assertThat(unconfirmedMembers.getContent()).hasSize(1);
        assertThat(unconfirmedMembers.getTotalElements()).isEqualTo(1);
        assertThat(unconfirmedMembers.getTotalPages()).isEqualTo(1);
        assertThat(unconfirmedMembers.getContent().get(0).getEmail()).isEqualTo("bob.johnson@example.com");
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
    void findMembersBySearchTermAndConfirmationStatus_shouldReturnMatchingConfirmedMembers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTermAndConfirmationStatus("alice", true, pageable);
        
        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getTotalElements()).isEqualTo(1);
        assertThat(searchResults.getContent().get(0).getFirstName()).isEqualTo("Alice");
        assertThat(searchResults.getContent().get(0).isConfirmed()).isTrue();
    }

    @Test
    void findMembersBySearchTermAndConfirmationStatus_shouldReturnMatchingUnconfirmedMembers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTermAndConfirmationStatus("bob", false, pageable);
        
        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getTotalElements()).isEqualTo(1);
        assertThat(searchResults.getContent().get(0).getFirstName()).isEqualTo("Bob");
        assertThat(searchResults.getContent().get(0).isConfirmed()).isFalse();
    }

    @Test
    void findMembersBySearchTermAndConfirmationStatus_shouldReturnEmptyWhenNoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> searchResults = memberRepository.findMembersBySearchTermAndConfirmationStatus("alice", false, pageable);
        
        assertThat(searchResults.getContent()).isEmpty();
        assertThat(searchResults.getTotalElements()).isEqualTo(0);
    }

    @Test
    void countByConfirmedTrue_shouldReturnCorrectCount() {
        long count = memberRepository.countByConfirmedTrue();
        assertThat(count).isEqualTo(2); // Alice et Charlie
    }

    @Test
    void countByConfirmedFalse_shouldReturnCorrectCount() {
        long count = memberRepository.countByConfirmedFalse();
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
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MemberEntity savedMember = memberRepository.save(newMember);
        entityManager.flush(); // Ensure it's written to DB
        entityManager.clear(); // Clear cache to read fresh from DB

        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("david.clark@example.com");
        assertThat(memberRepository.findById(savedMember.getId())).isPresent();
    }

    @Test
    void update_shouldModifyExistingMember() {
        member1.setFirstName("Alicia");
        member1.setConfirmed(false);
        member1.setConfirmedAt(null);
        member1.setUpdatedAt(LocalDateTime.now()); // Simuler une mise à jour manuelle

        memberRepository.save(member1);
        entityManager.flush();
        entityManager.clear();

        Optional<MemberEntity> updatedMember = memberRepository.findById(member1.getId());
        assertThat(updatedMember).isPresent();
        assertThat(updatedMember.get().getFirstName()).isEqualTo("Alicia");
        assertThat(updatedMember.get().isConfirmed()).isFalse();
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
        assertThat(memberRepository.findAll()).hasSize(2); // member2 et member3 devraient rester
    }
}