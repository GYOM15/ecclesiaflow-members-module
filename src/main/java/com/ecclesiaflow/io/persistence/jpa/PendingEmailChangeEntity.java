package com.ecclesiaflow.io.persistence.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_email_changes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingEmailChangeEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID memberId;

    @NotBlank
    @Column(name = "new_email", nullable = false)
    private String newEmail;

    @NotNull
    @Column(name = "token", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID token;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
