package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.Role;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import com.ecclesiaflow.io.persistence.jpa.SpringDataMemberRepository;
import com.ecclesiaflow.io.persistence.mappers.MemberPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MemberRepositoryImplTest {

    @Mock
    private SpringDataMemberRepository springDataRepo;

    @Mock
    private MemberPersistenceMapper mapper;

    @InjectMocks
    private MemberRepositoryImpl memberRepository;

    private UUID testId;
    private UUID testMemberId;
    private String testEmail;
    private MemberEntity testEntity;
    private Member testDomain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testId = UUID.randomUUID();
        testMemberId = UUID.randomUUID();
        testEmail = "test@example.com";
        LocalDateTime now = LocalDateTime.now();

        // Initialiser une entité et un domaine de test
        testEntity = MemberEntity.builder()
                .id(testId)
                .memberId(testMemberId)
                .firstName("John")
                .lastName("Doe")
                .email(testEmail)
                .address("123 Main St")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(now.minusDays(5))
                .updatedAt(now.minusHours(1))
                .build();

        testDomain = Member.builder()
                .id(testId)
                .memberId(testMemberId)
                .firstName("John")
                .lastName("Doe")
                .email(testEmail)
                .address("123 Main St")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(now.minusDays(5))
                .updatedAt(now.minusHours(1))
                .build();
    }

    @Test
    void findById_shouldReturnMappedDomainObject() {
        when(springDataRepo.findById(testId)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Optional<Member> result = memberRepository.findById(testId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDomain);
        verify(springDataRepo, times(1)).findById(testId);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenNotFound() {
        when(springDataRepo.findById(testId)).thenReturn(Optional.empty());

        Optional<Member> result = memberRepository.findById(testId);

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findById(testId);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByEmail_shouldReturnMappedDomainObject() {
        when(springDataRepo.findByEmail(testEmail)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Optional<Member> result = memberRepository.findByEmail(testEmail);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDomain);
        verify(springDataRepo, times(1)).findByEmail(testEmail);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findByEmail_shouldReturnEmptyOptionalWhenNotFound() {
        when(springDataRepo.findByEmail(testEmail)).thenReturn(Optional.empty());

        Optional<Member> result = memberRepository.findByEmail(testEmail);

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findByEmail(testEmail);
        verifyNoInteractions(mapper);
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        when(springDataRepo.existsByEmail(testEmail)).thenReturn(true);

        boolean result = memberRepository.existsByEmail(testEmail);

        assertThat(result).isTrue();
        verify(springDataRepo, times(1)).existsByEmail(testEmail);
        verifyNoInteractions(mapper);
    }

    @Test
    void existsByEmail_shouldReturnFalse() {
        when(springDataRepo.existsByEmail(testEmail)).thenReturn(false);

        boolean result = memberRepository.existsByEmail(testEmail);

        assertThat(result).isFalse();
        verify(springDataRepo, times(1)).existsByEmail(testEmail);
        verifyNoInteractions(mapper);
    }


    @Test
    void getByConfirmedStatus_shouldReturnEmptyPageWhenNoneFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(springDataRepo.findByConfirmed(true, pageable)).thenReturn(emptyPage);

        Page<Member> result = memberRepository.getByConfirmedStatus(true, pageable);

        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        verify(springDataRepo, times(1)).findByConfirmed(true, pageable);
    }

    @Test
    void getByConfirmedStatus_shouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);
        MemberEntity entity1 = createTestEntity();
        entity1.setEmail("john@test.com");
        entity1.setConfirmed(true);
        MemberEntity entity2 = createTestEntity();
        entity2.setEmail("jane@test.com");
        entity2.setConfirmed(true);
        
        List<MemberEntity> entities = List.of(entity1, entity2);
        Page<MemberEntity> entityPage = new PageImpl<>(entities, pageable, 5);
        
        Member member1 = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .address("123 Test St")
                .role(Role.MEMBER)
                .confirmed(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Member member2 = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@test.com")
                .address("456 Test Ave")
                .role(Role.MEMBER)
                .confirmed(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(springDataRepo.findByConfirmed(true, pageable)).thenReturn(entityPage);
        when(mapper.toDomain(entity1)).thenReturn(member1);
        when(mapper.toDomain(entity2)).thenReturn(member2);

        // When
        Page<Member> result = memberRepository.getByConfirmedStatus(true, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(member1, member2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);
        
        verify(springDataRepo, times(1)).findByConfirmed(true, pageable);
        verify(mapper, times(1)).toDomain(entity1);
        verify(mapper, times(1)).toDomain(entity2);
    }

    @Test
    void getMembersBySearchTerm_shouldReturnPagedResults() {
        // Given
        String searchTerm = "john";
        Pageable pageable = PageRequest.of(1, 3);
        
        MemberEntity entity = createTestEntity();
        entity.setFirstName("John");
        List<MemberEntity> entities = List.of(entity);
        Page<MemberEntity> entityPage = new PageImpl<>(entities, pageable, 10);
        
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .address("123 Test St")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(springDataRepo.findMembersBySearchTerm(searchTerm, pageable)).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(member);

        // When
        Page<Member> result = memberRepository.getMembersBySearchTerm(searchTerm, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(member);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(3);
        
        verify(springDataRepo, times(1)).findMembersBySearchTerm(searchTerm, pageable);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    void getMembersBySearchTerm_shouldThrowExceptionWhenPageableIsNull() {
        // When & Then
        assertThatThrownBy(() -> memberRepository.getMembersBySearchTerm("test", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pageable cannot be null");
        
        verifyNoInteractions(springDataRepo, mapper);
    }

    @Test
    void getMembersBySearchTermAndConfirmationStatus_shouldReturnPagedResults() {
        // Given
        String searchTerm = "test";
        Boolean confirmed = false;
        Pageable pageable = PageRequest.of(0, 5);
        
        MemberEntity entity = createTestEntity();
        entity.setEmail("test@test.com");
        entity.setConfirmed(false);
        List<MemberEntity> entities = List.of(entity);
        Page<MemberEntity> entityPage = new PageImpl<>(entities, pageable, 1);
        
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@test.com")
                .address("123 Test St")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(springDataRepo.findMembersBySearchTermAndConfirmationStatus(searchTerm, confirmed, pageable))
            .thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(member);

        // When
        Page<Member> result = memberRepository.getMembersBySearchTermAndConfirmationStatus(
            searchTerm, confirmed, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(member);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        
        verify(springDataRepo, times(1))
            .findMembersBySearchTermAndConfirmationStatus(searchTerm, confirmed, pageable);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    void getMembersBySearchTermAndConfirmationStatus_shouldThrowExceptionWhenPageableIsNull() {
        // When & Then
        assertThatThrownBy(() -> memberRepository.getMembersBySearchTermAndConfirmationStatus("test", true, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pageable cannot be null");
        
        verifyNoInteractions(springDataRepo, mapper);
    }

    @Test
    void countConfirmedMembers_shouldReturnCorrectCount() {
        long count = 3L;
        when(springDataRepo.countByConfirmedTrue()).thenReturn(count);

        long result = memberRepository.countConfirmedMembers();

        assertThat(result).isEqualTo(count);
        verify(springDataRepo, times(1)).countByConfirmedTrue();
        verifyNoInteractions(mapper);
    }

    @Test
    void countPendingConfirmations_shouldReturnCorrectCount() {
        long count = 2L;
        when(springDataRepo.countByConfirmedFalse()).thenReturn(count);

        long result = memberRepository.countPendingConfirmations();

        assertThat(result).isEqualTo(count);
        verify(springDataRepo, times(1)).countByConfirmedFalse();
        verifyNoInteractions(mapper);
    }

    @Test
    void save_shouldMapAndSaveAndReturnMappedDomainObject() {
        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        when(springDataRepo.save(testEntity)).thenReturn(testEntity); // Simuler que le repository renvoie l'entité sauvegardée
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Member result = memberRepository.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
        verify(mapper, times(1)).toEntity(testDomain);
        verify(springDataRepo, times(1)).save(testEntity);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void delete_shouldMapAndDelegateToDelete() {
        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        doNothing().when(springDataRepo).delete(testEntity);

        memberRepository.delete(testDomain);

        verify(mapper, times(1)).toEntity(testDomain);
        verify(springDataRepo, times(1)).delete(testEntity);
    }

    @Test
    void findAll_shouldReturnMappedDomainObjects() {
        List<MemberEntity> entities = Collections.singletonList(testEntity);
        List<Member> domains = Collections.singletonList(testDomain);

        when(springDataRepo.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        List<Member> result = memberRepository.findAll();

        assertThat(result).containsExactly(testDomain);
        verify(springDataRepo, times(1)).findAll();
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoneFound() {
        when(springDataRepo.findAll()).thenReturn(Collections.emptyList());

        List<Member> result = memberRepository.findAll();

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findAll();
        verifyNoInteractions(mapper);
    }

    // Helper methods for test data creation
    private MemberEntity createTestEntity() {
        return MemberEntity.builder()
                .id(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .address("123 Test St")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Member createTestDomain() {
        return Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .address("123 Test St")
                .role(Role.MEMBER)
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}