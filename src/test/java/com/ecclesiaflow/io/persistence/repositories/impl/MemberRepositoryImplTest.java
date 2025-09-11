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

import static org.assertj.core.api.Assertions.assertThat;
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
    void findByConfirmedStatus_shouldReturnMappedDomainObjects() {
        List<MemberEntity> entities = Collections.singletonList(testEntity);
        List<Member> domains = Collections.singletonList(testDomain);

        when(springDataRepo.findByConfirmed(false)).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        List<Member> result = memberRepository.findByConfirmedStatus(false);

        assertThat(result).containsExactly(testDomain);
        verify(springDataRepo, times(1)).findByConfirmed(false);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findByConfirmedStatus_shouldReturnEmptyListWhenNoneFound() {
        when(springDataRepo.findByConfirmed(true)).thenReturn(Collections.emptyList());

        List<Member> result = memberRepository.findByConfirmedStatus(true);

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findByConfirmed(true);
        verifyNoInteractions(mapper);
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
}