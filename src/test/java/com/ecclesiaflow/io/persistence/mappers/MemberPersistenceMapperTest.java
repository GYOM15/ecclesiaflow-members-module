package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberPersistenceMapperTest {

    private MemberPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MemberPersistenceMapper.class);
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectlyWhenActive() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        MemberEntity entity = MemberEntity.builder()
                .id(id)
                .memberId(memberId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .status(MemberStatus.ACTIVE)
                .confirmedAt(now.minusHours(1))
                .createdAt(now.minusDays(5))
                .updatedAt(now.minusMinutes(30))
                .build();

        // Act
        Member domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getMemberId()).isEqualTo(memberId);
        assertThat(domain.getFirstName()).isEqualTo("John");
        assertThat(domain.getLastName()).isEqualTo("Doe");
        assertThat(domain.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(domain.getAddress()).isEqualTo("123 Main St");
        assertThat(domain.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(domain.isConfirmed()).isTrue();
        assertThat(domain.getConfirmedAt()).isEqualTo(now.minusHours(1));
        assertThat(domain.getCreatedAt()).isEqualTo(now.minusDays(5));
        assertThat(domain.getUpdatedAt()).isEqualTo(now.minusMinutes(30));
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectlyWhenPending() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        MemberEntity entity = MemberEntity.builder()
                .id(id)
                .memberId(memberId)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .address("456 Elm St")
                .status(MemberStatus.PENDING)
                .confirmedAt(null)
                .createdAt(now.minusDays(3))
                .updatedAt(now.minusMinutes(15))
                .build();

        // Act
        Member domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getStatus()).isEqualTo(MemberStatus.PENDING);
        assertThat(domain.isConfirmed()).isFalse();
        assertThat(domain.getConfirmedAt()).isNull();
    }

    @Test
    void toDomainOrThrow_shouldThrowIllegalArgumentExceptionWhenEntityIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                mapper.toDomainOrThrow(null)
        );
        assertThat(thrown.getMessage()).isEqualTo("MemberEntity must not be null");
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectlyWhenActive() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Member domain = Member.builder()
                .id(id)
                .memberId(memberId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .address("789 Oak Ave")
                .status(MemberStatus.ACTIVE)
                .confirmedAt(now.minusHours(2))
                .createdAt(now.minusDays(7))
                .updatedAt(now.minusMinutes(45))
                .build();

        // Act
        MemberEntity entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getMemberId()).isEqualTo(memberId);
        assertThat(entity.getFirstName()).isEqualTo("Alice");
        assertThat(entity.getLastName()).isEqualTo("Smith");
        assertThat(entity.getEmail()).isEqualTo("alice.smith@example.com");
        assertThat(entity.getAddress()).isEqualTo("789 Oak Ave");
        assertThat(entity.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(entity.getConfirmedAt()).isEqualTo(now.minusHours(2));
        assertThat(entity.getCreatedAt()).isEqualTo(now.minusDays(7));
        assertThat(entity.getUpdatedAt()).isEqualTo(now.minusMinutes(45));
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectlyWhenPending() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Member domain = Member.builder()
                .id(id)
                .memberId(memberId)
                .firstName("Bob")
                .lastName("Brown")
                .email("bob.brown@example.com")
                .address("101 Cedar Ln")
                .status(MemberStatus.PENDING)
                .confirmedAt(null)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusMinutes(5))
                .build();

        // Act
        MemberEntity entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getStatus()).isEqualTo(MemberStatus.PENDING);
        assertThat(entity.getConfirmedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isEqualTo(now.minusMinutes(5));
    }

    @Test
    void toEntityOrThrow_shouldThrowIllegalArgumentExceptionWhenDomainIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                mapper.toEntityOrThrow(null)
        );
        assertThat(thrown.getMessage()).isEqualTo("Member domain object must not be null");
    }
}
