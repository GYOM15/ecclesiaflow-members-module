package com.ecclesiaflow.io.persistence.repositories.impl;

import com.ecclesiaflow.business.domain.emailchange.PendingEmailChange;
import com.ecclesiaflow.io.persistence.jpa.PendingEmailChangeEntity;
import com.ecclesiaflow.io.persistence.jpa.SpringDataPendingEmailChangeRepository;
import com.ecclesiaflow.io.persistence.mappers.PendingEmailChangePersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PendingEmailChangeRepositoryImpl")
class PendingEmailChangeRepositoryImplTest {

    @Mock
    private SpringDataPendingEmailChangeRepository springDataRepo;

    @Mock
    private PendingEmailChangePersistenceMapper mapper;

    @InjectMocks
    private PendingEmailChangeRepositoryImpl repository;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID TOKEN = UUID.randomUUID();

    private PendingEmailChange buildDomain() {
        return PendingEmailChange.builder()
                .id(ID)
                .memberId(MEMBER_ID)
                .newEmail("new@example.com")
                .token(TOKEN)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    private PendingEmailChangeEntity buildEntity() {
        return PendingEmailChangeEntity.builder()
                .id(ID)
                .memberId(MEMBER_ID)
                .newEmail("new@example.com")
                .token(TOKEN)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Test
    @DisplayName("save should map to entity, persist, and map back")
    void saveShouldWork() {
        PendingEmailChange domain = buildDomain();
        PendingEmailChangeEntity entity = buildEntity();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(springDataRepo.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        PendingEmailChange result = repository.save(domain);

        assertThat(result).isEqualTo(domain);
        verify(springDataRepo).save(entity);
    }

    @Test
    @DisplayName("getByToken should return mapped domain when found")
    void getByTokenFound() {
        PendingEmailChangeEntity entity = buildEntity();
        PendingEmailChange domain = buildDomain();

        when(springDataRepo.findByToken(TOKEN)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<PendingEmailChange> result = repository.getByToken(TOKEN);

        assertThat(result).isPresent().contains(domain);
    }

    @Test
    @DisplayName("getByToken should return empty when not found")
    void getByTokenNotFound() {
        when(springDataRepo.findByToken(TOKEN)).thenReturn(Optional.empty());

        Optional<PendingEmailChange> result = repository.getByToken(TOKEN);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getByMemberId should return mapped domain when found")
    void getByMemberIdFound() {
        PendingEmailChangeEntity entity = buildEntity();
        PendingEmailChange domain = buildDomain();

        when(springDataRepo.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<PendingEmailChange> result = repository.getByMemberId(MEMBER_ID);

        assertThat(result).isPresent().contains(domain);
    }

    @Test
    @DisplayName("getByMemberId should return empty when not found")
    void getByMemberIdNotFound() {
        when(springDataRepo.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

        Optional<PendingEmailChange> result = repository.getByMemberId(MEMBER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delete should map to entity and delete")
    void deleteShouldWork() {
        PendingEmailChange domain = buildDomain();
        PendingEmailChangeEntity entity = buildEntity();

        when(mapper.toEntity(domain)).thenReturn(entity);

        repository.delete(domain);

        verify(springDataRepo).delete(entity);
    }

    @Test
    @DisplayName("deleteByMemberId should delegate to spring data repo")
    void deleteByMemberIdShouldWork() {
        repository.deleteByMemberId(MEMBER_ID);

        verify(springDataRepo).deleteByMemberId(MEMBER_ID);
    }
}
