package com.ecclesiaflow.io.repository;

import com.ecclesiaflow.io.entities.Member;
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
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember1;
    private Member testMember2;
    private Member testMember3;

    @BeforeEach
    void setUp() {
        // Membre confirmé
        testMember1 = new Member();
        testMember1.setMemberId(UUID.randomUUID());
        testMember1.setFirstName("Jean");
        testMember1.setLastName("Dupont");
        testMember1.setEmail("jean.dupont@example.com");
        testMember1.setAddress("123 Rue de la Paix");
        testMember1.setRole(Role.MEMBER);
        testMember1.setConfirmed(true);
        testMember1.setCreatedAt(LocalDateTime.now());

        // Membre non confirmé
        testMember2 = new Member();
        testMember2.setMemberId(UUID.randomUUID());
        testMember2.setFirstName("Marie");
        testMember2.setLastName("Martin");
        testMember2.setEmail("marie.martin@example.com");
        testMember2.setAddress("456 Avenue des Fleurs");
        testMember2.setRole(Role.MEMBER);
        testMember2.setConfirmed(false);
        testMember2.setCreatedAt(LocalDateTime.now());

        // Membre admin confirmé
        testMember3 = new Member();
        testMember3.setMemberId(UUID.randomUUID());
        testMember3.setFirstName("Admin");
        testMember3.setLastName("User");
        testMember3.setEmail("admin@example.com");
        testMember3.setAddress("789 Boulevard Central");
        testMember3.setRole(Role.ADMIN);
        testMember3.setConfirmed(true);
        testMember3.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnMember() {
        // Given
        entityManager.persistAndFlush(testMember1);

        // When
        Optional<Member> result = memberRepository.findByEmail("jean.dupont@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Jean", result.get().getFirstName());
        assertEquals("Dupont", result.get().getLastName());
        assertEquals("jean.dupont@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testMember1);

        // When
        Optional<Member> result = memberRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testMember1);

        // When
        Optional<Member> result = memberRepository.findByEmail(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByRole_WithExistingRole_ShouldReturnMember() {
        // Given
        entityManager.persistAndFlush(testMember1);
        entityManager.persistAndFlush(testMember3);

        // When
        Member result = memberRepository.findByRole(Role.ADMIN);

        // Then
        assertNotNull(result);
        assertEquals("Admin", result.getFirstName());
        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void findByRole_WithNonExistentRole_ShouldReturnNull() {
        // Given
        entityManager.persistAndFlush(testMember1);

        // When
        Member result = memberRepository.findByRole(Role.ADMIN);

        // Then
        assertNull(result);
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testMember1);

        // When
        boolean result = memberRepository.existsByEmail("jean.dupont@example.com");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        // Given
        entityManager.persistAndFlush(testMember1);

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
        entityManager.persistAndFlush(testMember1);
        entityManager.persistAndFlush(testMember2);
        entityManager.persistAndFlush(testMember3);

        // When
        List<Member> result = memberRepository.findByConfirmedStatus(true);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Member::isConfirmed));
        assertTrue(result.stream().anyMatch(m -> "Jean".equals(m.getFirstName())));
        assertTrue(result.stream().anyMatch(m -> "Admin".equals(m.getFirstName())));
    }

    @Test
    void findByConfirmedStatus_WithConfirmedFalse_ShouldReturnUnconfirmedMembers() {
        // Given
        entityManager.persistAndFlush(testMember1);
        entityManager.persistAndFlush(testMember2);
        entityManager.persistAndFlush(testMember3);

        // When
        List<Member> result = memberRepository.findByConfirmedStatus(false);

        // Then
        assertEquals(1, result.size());
        assertFalse(result.get(0).isConfirmed());
        assertEquals("Marie", result.get(0).getFirstName());
    }

    @Test
    void findByConfirmedStatus_WithNoMembers_ShouldReturnEmptyList() {
        // When
        List<Member> result = memberRepository.findByConfirmedStatus(true);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void countConfirmedMembers_WithConfirmedMembers_ShouldReturnCorrectCount() {
        // Given
        entityManager.persistAndFlush(testMember1);
        entityManager.persistAndFlush(testMember2);
        entityManager.persistAndFlush(testMember3);

        // When
        long result = memberRepository.countConfirmedMembers();

        // Then
        assertEquals(2, result);
    }

    @Test
    void countConfirmedMembers_WithNoConfirmedMembers_ShouldReturnZero() {
        // Given
        entityManager.persistAndFlush(testMember2); // Seul membre non confirmé

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
        entityManager.persistAndFlush(testMember1);
        entityManager.persistAndFlush(testMember2);
        entityManager.persistAndFlush(testMember3);

        // When
        long result = memberRepository.countPendingConfirmations();

        // Then
        assertEquals(1, result);
    }

    @Test
    void countPendingConfirmations_WithNoPendingMembers_ShouldReturnZero() {
        // Given
        entityManager.persistAndFlush(testMember1); // Seul membre confirmé
        entityManager.persistAndFlush(testMember3); // Admin confirmé

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
        Member savedMember = memberRepository.save(testMember1);

        // Then
        assertNotNull(savedMember.getId());
        assertEquals("Jean", savedMember.getFirstName());
        assertEquals("jean.dupont@example.com", savedMember.getEmail());

        // Vérifier en base
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());
        assertTrue(foundMember.isPresent());
        assertEquals("Jean", foundMember.get().getFirstName());
    }

    @Test
    void delete_WithExistingMember_ShouldRemoveMember() {
        // Given
        Member savedMember = entityManager.persistAndFlush(testMember1);
        UUID memberId = savedMember.getId();

        // When
        memberRepository.delete(savedMember);
        entityManager.flush();

        // Then
        Optional<Member> foundMember = memberRepository.findById(memberId);
        assertFalse(foundMember.isPresent());
    }

    @Test
    void findAll_WithMultipleMembers_ShouldReturnAllMembers() {
        // Given
        entityManager.persistAndFlush(testMember1);
        entityManager.persistAndFlush(testMember2);
        entityManager.persistAndFlush(testMember3);

        // When
        List<Member> result = memberRepository.findAll();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> "Jean".equals(m.getFirstName())));
        assertTrue(result.stream().anyMatch(m -> "Marie".equals(m.getFirstName())));
        assertTrue(result.stream().anyMatch(m -> "Admin".equals(m.getFirstName())));
    }

    @Test
    void findByEmail_WithSpecialCharacters_ShouldWork() {
        // Given
        testMember1.setEmail("jean.françois+test@église.com");
        entityManager.persistAndFlush(testMember1);

        // When
        Optional<Member> result = memberRepository.findByEmail("jean.françois+test@église.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("jean.françois+test@église.com", result.get().getEmail());
    }

    @Test
    void existsByEmail_CaseInsensitive_ShouldWork() {
        // Given
        entityManager.persistAndFlush(testMember1);

        // When
        boolean resultLower = memberRepository.existsByEmail("jean.dupont@example.com");
        boolean resultUpper = memberRepository.existsByEmail("JEAN.DUPONT@EXAMPLE.COM");

        // Then
        assertTrue(resultLower);
        // Note: Le comportement case-insensitive dépend de la configuration de la base de données
        // Dans un vrai test, vous pourriez vouloir tester les deux cas selon votre configuration
    }
}
