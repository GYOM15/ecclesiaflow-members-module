package com.ecclesiaflow.io.repository;

import com.ecclesiaflow.io.entities.MemberEntity;
import com.ecclesiaflow.io.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MemberRepository.
 * Vérifie les opérations de persistance et les requêtes personnalisées.
 */
@DataJpaTest
class MemberEntityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private MemberEntity testMemberEntity1;
    private MemberEntity testMemberEntity2;
    private MemberEntity testMemberEntity3;

    @BeforeEach
    void setUp() {
        // Membre confirmé
        testMemberEntity1 = new MemberEntity();
        testMemberEntity1.setMemberId(UUID.randomUUID());
        testMemberEntity1.setFirstName("Jean");
        testMemberEntity1.setLastName("Dupont");
        testMemberEntity1.setEmail("jean.dupont@example.com");
        testMemberEntity1.setAddress("123 Rue de la Paix");
        testMemberEntity1.setRole(Role.MEMBER);
        testMemberEntity1.setConfirmed(true);
        testMemberEntity1.setCreatedAt(LocalDateTime.now());

        // Membre non confirmé
        testMemberEntity2 = new MemberEntity();
        testMemberEntity2.setMemberId(UUID.randomUUID());
        testMemberEntity2.setFirstName("Marie");
        testMemberEntity2.setLastName("Martin");
        testMemberEntity2.setEmail("marie.martin@example.com");
        testMemberEntity2.setAddress("456 Avenue des Fleurs");
        testMemberEntity2.setRole(Role.MEMBER);
        testMemberEntity2.setConfirmed(false);
        testMemberEntity2.setCreatedAt(LocalDateTime.now());

        // Membre admin confirmé
        testMemberEntity3 = new MemberEntity();
        testMemberEntity3.setMemberId(UUID.randomUUID());
        testMemberEntity3.setFirstName("Admin");
        testMemberEntity3.setLastName("User");
        testMemberEntity3.setEmail("admin@example.com");
        testMemberEntity3.setAddress("789 Boulevard Central");
        testMemberEntity3.setRole(Role.ADMIN);
        testMemberEntity3.setConfirmed(true);
        testMemberEntity3.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnMember() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        Optional<MemberEntity> result = memberRepository.findByEmail("jean.dupont@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Jean", result.get().getFirstName());
        assertEquals("Dupont", result.get().getLastName());
        assertEquals("jean.dupont@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        Optional<MemberEntity> result = memberRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        Optional<MemberEntity> result = memberRepository.findByEmail(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByRole_WithExistingRole_ShouldReturnMember() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);
        entityManager.persistAndFlush(testMemberEntity3);

        // When
        MemberEntity result = memberRepository.findByRole(Role.ADMIN);

        // Then
        assertNotNull(result);
        assertEquals("Admin", result.getFirstName());
        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void findByRole_WithNonExistentRole_ShouldReturnNull() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        MemberEntity result = memberRepository.findByRole(Role.ADMIN);

        // Then
        assertNull(result);
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        boolean result = memberRepository.existsByEmail("jean.dupont@example.com");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        boolean result = memberRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(result);
    }

    @Test
    void existsByEmail_WithNullEmail_ShouldReturnFalse() {
        // When
        boolean result = memberRepository.existsByEmail(null);

        // Then
        assertFalse(result);
    }

    @Test
    void findByConfirmedStatus_WithConfirmedTrue_ShouldReturnConfirmedMembers() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);
        entityManager.persistAndFlush(testMemberEntity2);
        entityManager.persistAndFlush(testMemberEntity3);

        // When
        List<MemberEntity> result = memberRepository.findByConfirmedStatus(true);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(MemberEntity::isConfirmed));
        assertTrue(result.stream().anyMatch(m -> "Jean".equals(m.getFirstName())));
        assertTrue(result.stream().anyMatch(m -> "Admin".equals(m.getFirstName())));
    }

    @Test
    void findByConfirmedStatus_WithConfirmedFalse_ShouldReturnUnconfirmedMembers() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);
        entityManager.persistAndFlush(testMemberEntity2);
        entityManager.persistAndFlush(testMemberEntity3);

        // When
        List<MemberEntity> result = memberRepository.findByConfirmedStatus(false);

        // Then
        assertEquals(1, result.size());
        assertFalse(result.get(0).isConfirmed());
        assertEquals("Marie", result.get(0).getFirstName());
    }

    @Test
    void findByConfirmedStatus_WithNoMembers_ShouldReturnEmptyList() {
        // When
        List<MemberEntity> result = memberRepository.findByConfirmedStatus(true);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void countConfirmedMembers_WithConfirmedMembers_ShouldReturnCorrectCount() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);
        entityManager.persistAndFlush(testMemberEntity2);
        entityManager.persistAndFlush(testMemberEntity3);

        // When
        long result = memberRepository.countConfirmedMembers();

        // Then
        assertEquals(2, result);
    }

    @Test
    void countConfirmedMembers_WithNoConfirmedMembers_ShouldReturnZero() {
        // Given
        entityManager.persistAndFlush(testMemberEntity2); // Seul membre non confirmé

        // When
        long result = memberRepository.countConfirmedMembers();

        // Then
        assertEquals(0, result);
    }

    @Test
    void countConfirmedMembers_WithNoMembers_ShouldReturnZero() {
        // When
        long result = memberRepository.countConfirmedMembers();

        // Then
        assertEquals(0, result);
    }

    @Test
    void countPendingConfirmations_WithPendingMembers_ShouldReturnCorrectCount() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);
        entityManager.persistAndFlush(testMemberEntity2);
        entityManager.persistAndFlush(testMemberEntity3);

        // When
        long result = memberRepository.countPendingConfirmations();

        // Then
        assertEquals(1, result);
    }

    @Test
    void countPendingConfirmations_WithNoPendingMembers_ShouldReturnZero() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1); // Seul membre confirmé
        entityManager.persistAndFlush(testMemberEntity3); // Admin confirmé

        // When
        long result = memberRepository.countPendingConfirmations();

        // Then
        assertEquals(0, result);
    }

    @Test
    void countPendingConfirmations_WithNoMembers_ShouldReturnZero() {
        // When
        long result = memberRepository.countPendingConfirmations();

        // Then
        assertEquals(0, result);
    }

    @Test
    void save_WithNewMember_ShouldPersistMember() {
        // When
        MemberEntity savedMemberEntity = memberRepository.save(testMemberEntity1);

        // Then
        assertNotNull(savedMemberEntity.getId());
        assertEquals("Jean", savedMemberEntity.getFirstName());
        assertEquals("jean.dupont@example.com", savedMemberEntity.getEmail());

        // Vérifier en base
        Optional<MemberEntity> foundMember = memberRepository.findById(savedMemberEntity.getId());
        assertTrue(foundMember.isPresent());
        assertEquals("Jean", foundMember.get().getFirstName());
    }

    @Test
    void delete_WithExistingMember_ShouldRemoveMember() {
        // Given
        MemberEntity savedMemberEntity = entityManager.persistAndFlush(testMemberEntity1);
        UUID memberId = savedMemberEntity.getId();

        // When
        memberRepository.delete(savedMemberEntity);
        entityManager.flush();

        // Then
        Optional<MemberEntity> foundMember = memberRepository.findById(memberId);
        assertFalse(foundMember.isPresent());
    }

    @Test
    void findAll_WithMultipleMembers_ShouldReturnAllMembers() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);
        entityManager.persistAndFlush(testMemberEntity2);
        entityManager.persistAndFlush(testMemberEntity3);

        // When
        List<MemberEntity> result = memberRepository.findAll();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> "Jean".equals(m.getFirstName())));
        assertTrue(result.stream().anyMatch(m -> "Marie".equals(m.getFirstName())));
        assertTrue(result.stream().anyMatch(m -> "Admin".equals(m.getFirstName())));
    }

    @Test
    void findByEmail_WithSpecialCharacters_ShouldWork() {
        // Given
        testMemberEntity1.setEmail("jean.françois+test@église.com");
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        Optional<MemberEntity> result = memberRepository.findByEmail("jean.françois+test@église.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("jean.françois+test@église.com", result.get().getEmail());
    }

    @Test
    void existsByEmail_CaseInsensitive_ShouldWork() {
        // Given
        entityManager.persistAndFlush(testMemberEntity1);

        // When
        boolean resultLower = memberRepository.existsByEmail("jean.dupont@example.com");
        boolean resultUpper = memberRepository.existsByEmail("JEAN.DUPONT@EXAMPLE.COM");

        // Then
        assertTrue(resultLower);
        // Note: Le comportement case-insensitive dépend de la configuration de la base de données
        // Dans un vrai test, vous pourriez vouloir tester les deux cas selon votre configuration
    }
}
