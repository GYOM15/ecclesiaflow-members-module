package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.emailchange.PendingEmailChange;
import com.ecclesiaflow.io.persistence.jpa.PendingEmailChangeEntity;
import org.springframework.stereotype.Component;

@Component
public class PendingEmailChangePersistenceMapper {

    public PendingEmailChange toDomain(PendingEmailChangeEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        return PendingEmailChange.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .newEmail(entity.getNewEmail())
                .token(entity.getToken())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    public PendingEmailChangeEntity toEntity(PendingEmailChange domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain object must not be null");
        }
        return PendingEmailChangeEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .newEmail(domain.getNewEmail())
                .token(domain.getToken())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getExpiresAt())
                .build();
    }
}
