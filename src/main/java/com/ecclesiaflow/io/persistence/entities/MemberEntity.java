package com.ecclesiaflow.io.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.ecclesiaflow.business.domain.member.Role;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "L’email est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Role role;

    @NotNull(message = "L’identifiant du membre est obligatoire")
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
    private UUID memberId;

    @NotBlank(message = "L’adresse est obligatoire")
    @Column(length = 200, nullable = false)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    @Column
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean passwordSet = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
