package com.ecclesiaflow.business.exceptions;

import com.ecclesiaflow.business.domain.member.SocialProvider;
import lombok.Getter;

@Getter
public class LocalCredentialsRequiredException extends RuntimeException {

    private final SocialProvider provider;

    public LocalCredentialsRequiredException(SocialProvider provider) {
        super("Local credentials required before changing email");
        this.provider = provider;
    }
}
