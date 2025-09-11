package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.Role;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberPersistenceMapperTest {

    private MemberPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MemberPersistenceMapper();
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectlyWhenConfirmed() {
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
                .role(Role.MEMBER)
                .confirmed(true)
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
        assertThat(domain.getRole()).isEqualTo(Role.MEMBER);
        assertThat(domain.isConfirmed()).isTrue();
        assertThat(domain.getConfirmedAt()).isEqualTo(now.minusHours(1));
        assertThat(domain.getCreatedAt()).isEqualTo(now.minusDays(5));
        assertThat(domain.getUpdatedAt()).isEqualTo(now.minusMinutes(30));
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectlyWhenNotConfirmed() {
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
                .role(Role.ADMIN)
                .confirmed(false)
                .confirmedAt(null)
                .createdAt(now.minusDays(3))
                .updatedAt(now.minusMinutes(15))
                .build();

        // Act
        Member domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.isConfirmed()).isFalse();
        assertThat(domain.getConfirmedAt()).isNull();
    }

    @Test
    void toDomain_shouldThrowIllegalArgumentExceptionWhenEntityIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                mapper.toDomain(null)
        );
        assertThat(thrown.getMessage()).isEqualTo("L'objet domaine ne peut pas être null");
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectlyWhenConfirmed() {
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
                .role(Role.MEMBER)
                .confirmed(true)
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
        assertThat(entity.getRole()).isEqualTo(Role.MEMBER);
        assertThat(entity.isConfirmed()).isTrue();
        assertThat(entity.getConfirmedAt()).isEqualTo(now.minusHours(2));
        assertThat(entity.getCreatedAt()).isEqualTo(now.minusDays(7));
        // updatedAt n'est pas mappé car géré par @UpdateTimestamp
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectlyWhenNotConfirmed() {
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
                .role(Role.MEMBER)
                .confirmed(false)
                .confirmedAt(null)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusMinutes(5))
                .build();

        // Act
        MemberEntity entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.isConfirmed()).isFalse();
        assertThat(entity.getConfirmedAt()).isNull();
        // updatedAt n'est pas mappé car géré par @UpdateTimestamp
        assertThat(entity.getUpdatedAt()).isNull();
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