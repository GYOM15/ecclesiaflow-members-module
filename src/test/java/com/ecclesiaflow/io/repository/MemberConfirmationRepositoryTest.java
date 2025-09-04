package com.ecclesiaflow.io.repository;

import com.ecclesiaflow.io.entities.MemberConfirmation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MemberConfirmationRepository.
 * Vérifie les opérations de persistance et les requêtes personnalisées pour les confirmations.
 */
@DataJpaTest
class MemberEntityConfirmationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberConfirmationRepository confirmationRepository;

    private MemberConfirmation activeConfirmation;
    private MemberConfirmation expiredConfirmation;
    private UUID testMemberId1;
    private UUID testMemberId2;

    @BeforeEach
    void setUp() {
        testMemberId1 = UUID.randomUUID();
        testMemberId2 = UUID.randomUUID();

        // Confirmation active (expire dans 1 heure)
        activeConfirmation = MemberConfirmation.builder()
                .memberId(testMemberId1)
                .code("123456")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        // Confirmation expirée (expirée il y a 1 heure)
        expiredConfirmation = MemberConfirmation.builder()
                .memberId(testMemberId2)
                .code("654321")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void findByMemberId_WithExistingMemberId_ShouldReturnConfirmation() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberId(testMemberId1);

        // Then
        assertTrue(result.isPresent());
        assertEquals("123456", result.get().getCode());
        assertEquals(testMemberId1, result.get().getMemberId());
    }

    @Test
    void findByMemberId_WithNonExistentMemberId_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberId(UUID.randomUUID());

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMemberId_WithNullMemberId_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberId(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMemberIdAndCode_WithValidMemberIdAndCode_ShouldReturnConfirmation() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberIdAndCode(testMemberId1, "123456");

        // Then
        assertTrue(result.isPresent());
        assertEquals("123456", result.get().getCode());
        assertEquals(testMemberId1, result.get().getMemberId());
    }

    @Test
    void findByMemberIdAndCode_WithWrongCode_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberIdAndCode(testMemberId1, "wrong_code");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMemberIdAndCode_WithWrongMemberId_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberIdAndCode(UUID.randomUUID(), "123456");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMemberIdAndCode_WithNullParameters_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result1 = confirmationRepository.findByMemberIdAndCode(null, "123456");
        Optional<MemberConfirmation> result2 = confirmationRepository.findByMemberIdAndCode(testMemberId1, null);
        Optional<MemberConfirmation> result3 = confirmationRepository.findByMemberIdAndCode(null, null);

        // Then
        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
        assertFalse(result3.isPresent());
    }

    @Test
    void findByCode_WithExistingCode_ShouldReturnConfirmation() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByCode("123456");

        // Then
        assertTrue(result.isPresent());
        assertEquals("123456", result.get().getCode());
        assertEquals(testMemberId1, result.get().getMemberId());
    }

    @Test
    void findByCode_WithNonExistentCode_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByCode("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByCode_WithNullCode_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByCode(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void deleteExpiredConfirmations_WithExpiredConfirmations_ShouldDeleteThem() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);
        entityManager.persistAndFlush(expiredConfirmation);
        LocalDateTime now = LocalDateTime.now();

        // When
        int deletedCount = confirmationRepository.deleteExpiredConfirmations(now);
        entityManager.flush();

        // Then
        assertEquals(1, deletedCount);
        
        // Vérifier que seule la confirmation active reste
        Optional<MemberConfirmation> activeResult = confirmationRepository.findByMemberId(testMemberId1);
        Optional<MemberConfirmation> expiredResult = confirmationRepository.findByMemberId(testMemberId2);
        
        assertTrue(activeResult.isPresent());
        assertFalse(expiredResult.isPresent());
    }

    @Test
    void deleteExpiredConfirmations_WithNoExpiredConfirmations_ShouldDeleteNothing() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);
        LocalDateTime now = LocalDateTime.now();

        // When
        int deletedCount = confirmationRepository.deleteExpiredConfirmations(now);

        // Then
        assertEquals(0, deletedCount);
        
        // Vérifier que la confirmation active reste
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberId(testMemberId1);
        assertTrue(result.isPresent());
    }

    @Test
    void deleteExpiredConfirmations_WithFutureDate_ShouldDeleteAllConfirmations() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);
        entityManager.persistAndFlush(expiredConfirmation);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        // When
        int deletedCount = confirmationRepository.deleteExpiredConfirmations(futureDate);
        entityManager.flush();

        // Then
        assertEquals(2, deletedCount);
        
        // Vérifier qu'aucune confirmation ne reste
        Optional<MemberConfirmation> activeResult = confirmationRepository.findByMemberId(testMemberId1);
        Optional<MemberConfirmation> expiredResult = confirmationRepository.findByMemberId(testMemberId2);
        
        assertFalse(activeResult.isPresent());
        assertFalse(expiredResult.isPresent());
    }

    @Test
    void countPendingConfirmations_WithActiveConfirmations_ShouldReturnCorrectCount() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);
        entityManager.persistAndFlush(expiredConfirmation);
        LocalDateTime now = LocalDateTime.now();

        // When
        long count = confirmationRepository.countPendingConfirmations(now);

        // Then
        assertEquals(1, count);
    }

    @Test
    void countPendingConfirmations_WithNoActiveConfirmations_ShouldReturnZero() {
        // Given
        entityManager.persistAndFlush(expiredConfirmation);
        LocalDateTime now = LocalDateTime.now();

        // When
        long count = confirmationRepository.countPendingConfirmations(now);

        // Then
        assertEquals(0, count);
    }

    @Test
    void countPendingConfirmations_WithNoConfirmations_ShouldReturnZero() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        long count = confirmationRepository.countPendingConfirmations(now);

        // Then
        assertEquals(0, count);
    }

    @Test
    void existsByMemberId_WithExistingMemberId_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        boolean exists = confirmationRepository.existsByMemberId(testMemberId1);

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByMemberId_WithNonExistentMemberId_ShouldReturnFalse() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        boolean exists = confirmationRepository.existsByMemberId(UUID.randomUUID());

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByMemberId_WithNullMemberId_ShouldReturnFalse() {
        // Given
        entityManager.persistAndFlush(activeConfirmation);

        // When
        boolean exists = confirmationRepository.existsByMemberId(null);

        // Then
        assertFalse(exists);
    }

    @Test
    void save_WithNewConfirmation_ShouldPersistConfirmation() {
        // Given
        MemberConfirmation newConfirmation = MemberConfirmation.builder()
                .memberId(UUID.randomUUID())
                .code("789012")
                .expiresAt(LocalDateTime.now().plusHours(2))
                .build();

        // When
        MemberConfirmation saved = confirmationRepository.save(newConfirmation);

        // Then
        assertNotNull(saved.getId());
        assertEquals("789012", saved.getCode());
        assertNotNull(saved.getMemberId());

        // Vérifier en base
        Optional<MemberConfirmation> found = confirmationRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("789012", found.get().getCode());
    }

    @Test
    void delete_WithExistingConfirmation_ShouldRemoveConfirmation() {
        // Given
        MemberConfirmation saved = entityManager.persistAndFlush(activeConfirmation);
        UUID confirmationId = saved.getId();

        // When
        confirmationRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<MemberConfirmation> found = confirmationRepository.findById(confirmationId);
        assertFalse(found.isPresent());
    }

    @Test
    void findByCode_WithUniqueCode_ShouldReturnOne() {
        // Given
        MemberConfirmation confirmation1 = MemberConfirmation.builder()
                .memberId(testMemberId1)
                .code("111111") // Code unique
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        // When
        entityManager.persistAndFlush(confirmation1);
        Optional<MemberConfirmation> result = confirmationRepository.findByCode("111111");

        // Then
        assertTrue(result.isPresent());
        assertEquals("111111", result.get().getCode());
    }

    @Test
    void findByMemberIdAndCode_WithExpiredConfirmation_ShouldStillReturnIt() {
        // Given
        entityManager.persistAndFlush(expiredConfirmation);

        // When
        Optional<MemberConfirmation> result = confirmationRepository.findByMemberIdAndCode(testMemberId2, "654321");

        // Then
        assertTrue(result.isPresent());
        assertEquals("654321", result.get().getCode());
        assertTrue(result.get().getExpiresAt().isBefore(LocalDateTime.now()));
    }
}
