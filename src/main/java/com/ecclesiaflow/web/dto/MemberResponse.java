package com.ecclesiaflow.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {
    private String message;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String password;
    private String role;
    private boolean accountNonLocked;
    private boolean enabled;
    private String token;
    private String username;
    private boolean confirmed;
    private String createdAt;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
}

