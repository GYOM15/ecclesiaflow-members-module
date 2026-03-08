package com.ecclesiaflow.io.persistence.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.ecclesiaflow.business.domain.member.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/** JPA entity for the {@code member} table. */
@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Member ID is required")
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
    private UUID memberId;

    @NotBlank(message = "Address is required")
    @Column(length = 200, nullable = false)
    private String address;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.PENDING;

    @Column
    private LocalDateTime confirmedAt;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
