package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.io.persistence.jpa.MemberConfirmationEntity;
import com.ecclesiaflow.io.persistence.jpa.SpringDataMemberConfirmationRepository;
import com.ecclesiaflow.io.persistence.mappers.MemberConfirmationPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MemberConfirmationRepositoryImplTest {

    @Mock
    private SpringDataMemberConfirmationRepository springDataRepo;

    @Mock
    private MemberConfirmationPersistenceMapper mapper;

    @InjectMocks
    private MemberConfirmationRepositoryImpl memberConfirmationRepository;

    private UUID testMemberId;
    private String testCode;
    private MemberConfirmationEntity testEntity;
    private MemberConfirmation testDomain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testMemberId = UUID.randomUUID();
        testCode = "123456";
        LocalDateTime now = LocalDateTime.now();

        // Initialiser une entité et un domaine de test
        testEntity = MemberConfirmationEntity.builder()
                .id(UUID.randomUUID())
                .memberId(testMemberId)
                .code(testCode)
                .createdAt(now.minusHours(1))
                .expiresAt(now.plusDays(1))
                .build();

        testDomain = MemberConfirmation.builder()
                .id(testEntity.getId())
                .memberId(testMemberId)
                .code(testCode)
                .createdAt(testEntity.getCreatedAt())
                .expiresAt(testEntity.getExpiresAt())
                .build();
    }

    @Test
    void findByMemberId_shouldReturnMappedDomainObject() {
        when(springDataRepo.findByMemberId(testMemberId)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Optional<MemberConfirmation> result = memberConfirmationRepository.findByMemberId(testMemberId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDomain);
        verify(springDataRepo, times(1)).findByMemberId(testMemberId);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findByMemberId_shouldReturnEmptyOptionalWhenNotFound() {
        when(springDataRepo.findByMemberId(testMemberId)).thenReturn(Optional.empty());

        Optional<MemberConfirmation> result = memberConfirmationRepository.findByMemberId(testMemberId);

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findByMemberId(testMemberId);
        verifyNoInteractions(mapper); // Mapper ne devrait pas être appelé si Optional est vide
    }

    @Test
    void findByMemberIdAndCode_shouldReturnMappedDomainObject() {
        when(springDataRepo.findByMemberIdAndCode(testMemberId, testCode)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Optional<MemberConfirmation> result = memberConfirmationRepository.findByMemberIdAndCode(testMemberId, testCode);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDomain);
        verify(springDataRepo, times(1)).findByMemberIdAndCode(testMemberId, testCode);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findByMemberIdAndCode_shouldReturnEmptyOptionalWhenNotFound() {
        when(springDataRepo.findByMemberIdAndCode(testMemberId, testCode)).thenReturn(Optional.empty());

        Optional<MemberConfirmation> result = memberConfirmationRepository.findByMemberIdAndCode(testMemberId, testCode);

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findByMemberIdAndCode(testMemberId, testCode);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByCode_shouldReturnMappedDomainObject() {
        when(springDataRepo.findByCode(testCode)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Optional<MemberConfirmation> result = memberConfirmationRepository.findByCode(testCode);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDomain);
        verify(springDataRepo, times(1)).findByCode(testCode);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findByCode_shouldReturnEmptyOptionalWhenNotFound() {
        when(springDataRepo.findByCode(testCode)).thenReturn(Optional.empty());

        Optional<MemberConfirmation> result = memberConfirmationRepository.findByCode(testCode);

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findByCode(testCode);
        verifyNoInteractions(mapper);
    }

    @Test
    void existsByMemberId_shouldReturnTrue() {
        when(springDataRepo.existsByMemberId(testMemberId)).thenReturn(true);

        boolean result = memberConfirmationRepository.existsByMemberId(testMemberId);

        assertThat(result).isTrue();
        verify(springDataRepo, times(1)).existsByMemberId(testMemberId);
        verifyNoInteractions(mapper);
    }

    @Test
    void existsByMemberId_shouldReturnFalse() {
        when(springDataRepo.existsByMemberId(testMemberId)).thenReturn(false);

        boolean result = memberConfirmationRepository.existsByMemberId(testMemberId);

        assertThat(result).isFalse();
        verify(springDataRepo, times(1)).existsByMemberId(testMemberId);
        verifyNoInteractions(mapper);
    }

    @Test
    void findExpiredConfirmations_shouldReturnMappedDomainObjects() {
        List<MemberConfirmationEntity> entities = Collections.singletonList(testEntity);
        List<MemberConfirmation> domains = Collections.singletonList(testDomain);

        // Capture LocalDateTime.now() passed to springDataRepo
        when(springDataRepo.findExpiredConfirmations(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        List<MemberConfirmation> result = memberConfirmationRepository.findExpiredConfirmations();

        assertThat(result).containsExactly(testDomain);
        // Verify with ArgumentMatchers.any() because LocalDateTime.now() is called inside the method
        verify(springDataRepo, times(1)).findExpiredConfirmations(ArgumentMatchers.any(LocalDateTime.class));
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void findExpiredConfirmations_shouldReturnEmptyListWhenNoneFound() {
        when(springDataRepo.findExpiredConfirmations(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<MemberConfirmation> result = memberConfirmationRepository.findExpiredConfirmations();

        assertThat(result).isEmpty();
        verify(springDataRepo, times(1)).findExpiredConfirmations(ArgumentMatchers.any(LocalDateTime.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void countPendingConfirmations_shouldReturnCorrectCount() {
        long count = 5L;
        // Capture LocalDateTime.now() passed to springDataRepo
        when(springDataRepo.countPendingConfirmations(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(count);

        long result = memberConfirmationRepository.countPendingConfirmations();

        assertThat(result).isEqualTo(count);
        // Verify with ArgumentMatchers.any() because LocalDateTime.now() is called inside the method
        verify(springDataRepo, times(1)).countPendingConfirmations(ArgumentMatchers.any(LocalDateTime.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void save_shouldMapAndSaveAndReturnMappedDomainObject() {
        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        when(springDataRepo.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        MemberConfirmation result = memberConfirmationRepository.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
        verify(mapper, times(1)).toEntity(testDomain);
        verify(springDataRepo, times(1)).save(testEntity);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    void delete_shouldMapAndDelegateToDelete() {
        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        doNothing().when(springDataRepo).delete(testEntity);

        memberConfirmationRepository.delete(testDomain);

        verify(mapper, times(1)).toEntity(testDomain);
        verify(springDataRepo, times(1)).delete(testEntity);
    }

    @Test
    void deleteExpiredConfirmations_shouldDelegateToDeleteAndReturnCount() {
        int deletedCount = 3;
        // Capture LocalDateTime.now() passed to springDataRepo
        when(springDataRepo.deleteExpiredConfirmations(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(deletedCount);

        int result = memberConfirmationRepository.deleteExpiredConfirmations();

        assertThat(result).isEqualTo(deletedCount);
        // Verify with ArgumentMatchers.any() because LocalDateTime.now() is called inside the method
        verify(springDataRepo, times(1)).deleteExpiredConfirmations(ArgumentMatchers.any(LocalDateTime.class));
        verifyNoInteractions(mapper);
    }
}