package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.io.persistence.jpa.MemberEntity;
import org.mapstruct.Mapper;

/** MapStruct mapper for Member domain object and MemberEntity JPA entity conversion. */
@Mapper(componentModel = "spring")
public interface MemberPersistenceMapper {

    Member toDomain(MemberEntity entity);

    MemberEntity toEntity(Member domain);

    default Member toDomainOrThrow(MemberEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("MemberEntity must not be null");
        }
        return toDomain(entity);
    }

    default MemberEntity toEntityOrThrow(Member domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Member domain object must not be null");
        }
        return toEntity(domain);
    }
}
