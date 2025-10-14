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
    private UUID token1;
    private UUID token2;

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


        token1 = UUID.randomUUID();
        confirmation1 = MemberConfirmationEntity.builder()
                .memberId(testMember.getMemberId())
                .token(token1)
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        entityManager.persistAndFlush(confirmation1);

        token2 = UUID.randomUUID();
        confirmation2 = MemberConfirmationEntity.builder()
                .memberId(UUID.randomUUID())
                .token(token2)
                .createdAt(LocalDateTime.now().minusHours(2))
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        entityManager.persistAndFlush(confirmation2);

        entityManager.clear();
    }

    @Test
    void getByMemberId_shouldReturnConfirmation() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberId(testMember.getMemberId());
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo(confirmation1.getToken());
    }

    @Test
    void getByMemberId_shouldReturnEmptyWhenNotFound() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberId(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void getByMemberIdAndToken_shouldReturnConfirmation() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberIdAndToken(testMember.getMemberId(), token1);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(confirmation1.getId());
    }

    @Test
    void getByMemberIdAndToken_shouldReturnEmptyWhenMemberIdMismatch() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberIdAndToken(UUID.randomUUID(), token1);
        assertThat(found).isEmpty();
    }

    @Test
    void getByMemberIdAndToken_shouldReturnEmptyWhenTokenMismatch() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByMemberIdAndToken(testMember.getMemberId(), UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void getByToken_shouldReturnConfirmation() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByToken(token1);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(confirmation1.getId());
    }

    @Test
    void getByToken_shouldReturnEmptyWhenNotFound() {
        Optional<MemberConfirmationEntity> found = confirmationRepository.findByToken(UUID.randomUUID());
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
    void getExpiredConfirmations_shouldReturnOnlyExpired() {
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
                .token(UUID.randomUUID())
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
        UUID newToken = UUID.randomUUID();
        MemberConfirmationEntity newConfirmation = MemberConfirmationEntity.builder()
                .memberId(testMember.getMemberId())
                .token(newToken)
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        MemberConfirmationEntity savedConfirmation = confirmationRepository.save(newConfirmation);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedConfirmation).isNotNull();
        assertThat(savedConfirmation.getId()).isNotNull();
        assertThat(savedConfirmation.getToken()).isEqualTo(newToken);
        assertThat(savedConfirmation.getCreatedAt()).isNotNull();
        assertThat(confirmationRepository.findById(savedConfirmation.getId())).isPresent();
    }
}