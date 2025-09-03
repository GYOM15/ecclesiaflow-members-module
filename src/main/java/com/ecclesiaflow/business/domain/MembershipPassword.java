package com.ecclesiaflow.business.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MembershipPassword {
    private String email;
    private String password;
}
