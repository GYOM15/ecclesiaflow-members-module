package com.ecclesiaflow.business.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Member {

    private final UUID memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private final Role role = Role.MEMBER;

    @Builder.Default
    private boolean confirmed = false;

    @Builder.Default
    private boolean passwordSet = false;

    @Getter
    private final UUID id;

    public Member withUpdatedFields(MembershipUpdate update) {
        return this.toBuilder()
                .firstName(chooseUpdatedValue(update.getFirstName(), this.firstName))
                .lastName(chooseUpdatedValue(update.getLastName(), this.lastName))
                .email(chooseUpdatedValue(update.getEmail(), this.email))
                .address(chooseUpdatedValue(update.getAddress(), this.address))
                .createdAt(this.createdAt)
                .confirmedAt(this.confirmedAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private <T> T chooseUpdatedValue(T newValue, T currentValue) {
        return Optional.ofNullable(newValue).orElse(currentValue);
    }

    public void confirm() {
        if (this.confirmed) throw new IllegalStateException("Le membre est déjà confirmé.");
        this.confirmed = true;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markPasswordAsSet() {
        if (this.passwordSet) throw new IllegalStateException("Mot de passe déjà défini.");
        this.passwordSet = true;
    }
}