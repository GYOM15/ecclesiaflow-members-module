package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.io.persistence.jpa.MemberConfirmationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberConfirmationPersistenceMapperTest {

    private MemberConfirmationPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MemberConfirmationPersistenceMapper();
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectly() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        String code = "123456";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        MemberConfirmationEntity entity = MemberConfirmationEntity.builder()
                .id(id)
                .memberId(memberId)
                .code(code)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        // Act
        MemberConfirmation domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getMemberId()).isEqualTo(memberId);
        assertThat(domain.getCode()).isEqualTo(code);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void toDomain_shouldThrowIllegalArgumentExceptionWhenEntityIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                mapper.toDomain(null)
        );
        assertThat(thrown.getMessage()).isEqualTo("L'entité ne peut pas être null");
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectly() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        String code = "654321";
        LocalDateTime createdAt = LocalDateTime.now().minusHours(5);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(12);

        MemberConfirmation domain = MemberConfirmation.builder()
                .id(id)
                .memberId(memberId)
                .code(code)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        // Act
        MemberConfirmationEntity entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getMemberId()).isEqualTo(memberId);
        assertThat(entity.getCode()).isEqualTo(code);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void toEntity_shouldThrowIllegalArgumentExceptionWhenDomainIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                mapper.toEntity(null)
        );
        assertThat(thrown.getMessage()).isEqualTo("L'objet domaine ne peut pas être null");
    }
}