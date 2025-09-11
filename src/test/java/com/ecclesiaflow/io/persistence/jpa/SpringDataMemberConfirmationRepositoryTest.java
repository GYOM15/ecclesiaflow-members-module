package com.ecclesiaflow.io.persistence.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataMemberConfirmationRepositoryTest {

    @Autowired
    private SpringDataMemberConfirmationRepository confirmationRepository;

    @Autowired
    private SpringDataMemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private MemberEntity testMember;
    private MemberConfirmationEntity confirmation1;
    private MemberConfirmationEntity confirmation2;

    @BeforeEach
    void setUp() {
        confirmationRepository.deleteAll();
        memberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        testMember = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St, Anytown")
                .confirmed(false)
                .role(com.ecclesiaflow.business.domain.member.Role.MEMBER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(testMember);


        confirmation1 = MemberConfirmationEntity.builder()
                .memberId(testMember.getMemberId())
                .code("CODE1A")
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        entityManager.persistAndFlush(confirmation1);

        confirmation2 = MemberConfirmationEntity.builder()
                .memberId(UUID.randomUUID())
                .code("CODE2B")
                .createdAt(LocalDateTime.now().minusHours(2))
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        entityManager.persistAndFlush(confirmation2);

        entityManager.clear();
    }

    @Test
    void findByMemberId_shouldReturnConfirmation() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberId(testMember.getMemberId());
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo(confirmation1.getCode());
    }

    @Test
    void findByMemberId_shouldReturnEmptyWhenNotFound() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberId(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void findByMemberIdAndCode_shouldReturnConfirmation() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberIdAndCode(testMember.getMemberId(), "CODE1A");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(confirmation1.getId());
    }

    @Test
    void findByMemberIdAndCode_shouldReturnEmptyWhenMemberIdMismatch() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberIdAndCode(UUID.randomUUID(), "CODE1A");
        assertThat(found).isEmpty();
    }

    @Test
    void findByMemberIdAndCode_shouldReturnEmptyWhenCodeMismatch() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberIdAndCode(testMember.getMemberId(), "WRONGCODE");
        assertThat(found).isEmpty();
    }

    @Test
    void findByCode_shouldReturnConfirmation() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByCode("CODE1A");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(confirmation1.getId());
    }

    @Test
    void findByCode_shouldReturnEmptyWhenNotFound() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByCode("NONEXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByMemberId_shouldReturnTrue() {
        boolean exists = confirmationRepository.existsByMemberId(testMember.getMemberId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByMemberId_shouldReturnFalseWhenNotFound() {
        boolean exists = confirmationRepository.existsByMemberId(UUID.randomUUID());
        assertThat(exists).isFalse();
    }

    @Test
    void findExpiredConfirmations_shouldReturnOnlyExpired() {
        // Confirmation2 est expirée, confirmation1 ne l'est pas
        List<MemberConfirmationEntity> expired = confirmationRepository.findExpiredConfirmations(LocalDateTime.now());
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getId()).isEqualTo(confirmation2.getId());
    }

    @Test
    void countPendingConfirmations_shouldReturnCorrectCount() {
        // Confirmation1 est pending (non expirée), confirmation2 est expirée
        long count = confirmationRepository.countPendingConfirmations(LocalDateTime.now());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteExpiredConfirmations_shouldDeleteOnlyExpired() {
        // Confirmation2 est expirée, confirmation1 ne l'est pas
        int deletedCount = confirmationRepository.deleteExpiredConfirmations(LocalDateTime.now());
        assertThat(deletedCount).isEqualTo(1);
        assertThat(confirmationRepository.findAll()).hasSize(1);
        assertThat(confirmationRepository.findById(confirmation1.getId())).isPresent();
        assertThat(confirmationRepository.findById(confirmation2.getId())).isEmpty();
    }

    @Test
    void deleteByMemberId_shouldDeleteConfirmationsForSpecificMember() {
        // Créer une confirmation pour un autre membre pour s'assurer que seul le testMember est affecté
        MemberEntity anotherMember = MemberEntity.builder()
                .memberId(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .address("456 Oak Ave, Otherville")
                .confirmed(false)
                .role(com.ecclesiaflow.business.domain.member.Role.MEMBER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(anotherMember);

        MemberConfirmationEntity anotherConfirmation = MemberConfirmationEntity.builder()
                .memberId(anotherMember.getMemberId())
                .code("CODE3C")
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        entityManager.persistAndFlush(anotherConfirmation);
        entityManager.clear();

        assertThat(confirmationRepository.findAll()).hasSize(3);


        int deletedCount = confirmationRepository.deleteByMemberId(testMember.getMemberId());
        entityManager.flush();
        entityManager.clear();
        assertThat(deletedCount).isEqualTo(1);

        assertThat(confirmationRepository.findAll()).hasSize(2);
        assertThat(confirmationRepository.findById(confirmation1.getId())).isEmpty();
        assertThat(confirmationRepository.findById(confirmation2.getId())).isPresent();
        assertThat(confirmationRepository.findById(anotherConfirmation.getId())).isPresent();
    }

    @Test
    void save_shouldPersistNewConfirmation() {
        MemberConfirmationEntity newConfirmation = MemberConfirmationEntity.builder()
                .memberId(testMember.getMemberId())
                .code("NEWCDE")
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        MemberConfirmationEntity savedConfirmation = confirmationRepository.save(newConfirmation);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedConfirmation).isNotNull();
        assertThat(savedConfirmation.getId()).isNotNull();
        assertThat(savedConfirmation.getCode()).isEqualTo("NEWCDE");
        assertThat(savedConfirmation.getCreatedAt()).isNotNull();
        assertThat(confirmationRepository.findById(savedConfirmation.getId())).isPresent();
    }
}