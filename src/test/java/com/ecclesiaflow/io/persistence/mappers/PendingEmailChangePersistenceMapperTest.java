package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.emailchange.PendingEmailChange;
import com.ecclesiaflow.io.persistence.jpa.PendingEmailChangeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PendingEmailChangePersistenceMapper")
class PendingEmailChangePersistenceMapperTest {

    private final PendingEmailChangePersistenceMapper mapper = new PendingEmailChangePersistenceMapper();

    private static final UUID ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID TOKEN = UUID.randomUUID();
    private static final String NEW_EMAIL = "new@example.com";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 3, 15, 10, 0);
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 3, 16, 10, 0);

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map all fields from entity to domain")
        void shouldMapAllFields() {
            PendingEmailChangeEntity entity = PendingEmailChangeEntity.builder()
                    .id(ID)
                    .memberId(MEMBER_ID)
                    .newEmail(NEW_EMAIL)
                    .token(TOKEN)
                    .createdAt(CREATED_AT)
                    .expiresAt(EXPIRES_AT)
                    .build();

            PendingEmailChange domain = mapper.toDomain(entity);

            assertThat(domain.getId()).isEqualTo(ID);
            assertThat(domain.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(domain.getNewEmail()).isEqualTo(NEW_EMAIL);
            assertThat(domain.getToken()).isEqualTo(TOKEN);
            assertThat(domain.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(domain.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }

        @Test
        @DisplayName("should throw on null entity")
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> mapper.toDomain(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entity must not be null");
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map all fields from domain to entity")
        void shouldMapAllFields() {
            PendingEmailChange domain = PendingEmailChange.builder()
                    .id(ID)
                    .memberId(MEMBER_ID)
                    .newEmail(NEW_EMAIL)
                    .token(TOKEN)
                    .createdAt(CREATED_AT)
                    .expiresAt(EXPIRES_AT)
                    .build();

            PendingEmailChangeEntity entity = mapper.toEntity(domain);

            assertThat(entity.getId()).isEqualTo(ID);
            assertThat(entity.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(entity.getNewEmail()).isEqualTo(NEW_EMAIL);
            assertThat(entity.getToken()).isEqualTo(TOKEN);
            assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(entity.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }

        @Test
        @DisplayName("should throw on null domain object")
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> mapper.toEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Domain object must not be null");
        }
    }

    @Test
    @DisplayName("should be symmetric: domain -> entity -> domain")
    void shouldBeSymmetric() {
        PendingEmailChange original = PendingEmailChange.builder()
                .id(ID)
                .memberId(MEMBER_ID)
                .newEmail(NEW_EMAIL)
                .token(TOKEN)
                .createdAt(CREATED_AT)
                .expiresAt(EXPIRES_AT)
                .build();

        PendingEmailChange roundTripped = mapper.toDomain(mapper.toEntity(original));

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getMemberId()).isEqualTo(original.getMemberId());
        assertThat(roundTripped.getNewEmail()).isEqualTo(original.getNewEmail());
        assertThat(roundTripped.getToken()).isEqualTo(original.getToken());
    }
}
