package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.SocialProvider;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.SocialOnboardingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/** Maps domain {@link Member} objects to OpenAPI-generated response DTOs. */
@Component
@RequiredArgsConstructor
public class OpenApiModelMapper {

    @Value("${ecclesiaflow.auth-module.base-url:http://localhost:8081}")
    private String authModuleBaseUrl;

    /** Creates a {@link SignUpResponse} from a domain Member and a status message. */
    public SignUpResponse createSignUpResponse(Member member, String message) {
        SignUpResponse response = new SignUpResponse();
        response.setMessage(message);
        
        if (member != null) {
            populateSignUpResponseFromMember(member, response);
        }
        
        return response;
    }

    private void populateSignUpResponseFromMember(Member member, SignUpResponse response) {
        response.setEmail(member.getEmail());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setAddress(member.getAddress());
        response.setConfirmed(member.isConfirmed());
        response.setHasLocalCredentials(member.isHasLocalCredentials());

        if (member.getSocialProvider() != null) {
            response.setSocialProvider(mapSocialProvider(member.getSocialProvider()));
        }
        if (member.getCreatedAt() != null) {
            response.setCreatedAt(member.getCreatedAt().toString());
        }
        if (member.getConfirmedAt() != null) {
            response.setConfirmedAt(member.getConfirmedAt().toString());
        }
    }

    private SignUpResponse.SocialProviderEnum mapSocialProvider(SocialProvider provider) {
        return switch (provider) {
            case GOOGLE -> SignUpResponse.SocialProviderEnum.GOOGLE;
            case MICROSOFT -> SignUpResponse.SocialProviderEnum.MICROSOFT;
            case FACEBOOK -> SignUpResponse.SocialProviderEnum.FACEBOOK;
        };
    }

    /** Converts a paginated result of Members into a {@link MemberPageResponse}. */
    public MemberPageResponse createMemberPageResponse(Page<Member> memberPage) {
        MemberPageResponse response = new MemberPageResponse();
        
        List<SignUpResponse> content = memberPage.getContent().stream()
            .map(member -> {
                SignUpResponse memberResponse = new SignUpResponse();
                populateSignUpResponseFromMember(member, memberResponse);

                return memberResponse;
            })
            .collect(Collectors.toList());
        
        response.setContent(content);
        response.setPage(memberPage.getNumber());
        response.setNumber(memberPage.getNumber());
        response.setSize(memberPage.getSize());
        response.setTotalElements(memberPage.getTotalElements());
        response.setTotalPages(memberPage.getTotalPages());
        response.setFirst(memberPage.isFirst());
        response.setLast(memberPage.isLast());
        response.setNumberOfElements(memberPage.getNumberOfElements());
        response.setEmpty(memberPage.isEmpty());
        
        return response;
    }

    /** Maps a Member domain object to a SocialOnboardingResponse DTO. */
    public SocialOnboardingResponse createSocialOnboardingResponse(Member member) {
        SocialOnboardingResponse response = new SocialOnboardingResponse();
        response.setMessage("Profile successfully created via social login");
        response.setEmail(member.getEmail());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setAddress(member.getAddress());
        response.setPhoneNumber(member.getPhoneNumber());
        response.setConfirmed(true);
        response.setCreatedAt(member.getCreatedAt());
        response.setConfirmedAt(member.getConfirmedAt());
        return response;
    }

    /** Converts a {@link MembershipConfirmationResult} into a {@link ConfirmationResponse}. */
    public ConfirmationResponse createConfirmationResponse(MembershipConfirmationResult result) {
        ConfirmationResponse response = new ConfirmationResponse();
        
        if (result != null) {
            response.setMessage(result.getMessage());
            response.setTemporaryToken(result.getTemporaryToken());
            response.setExpiresIn(result.getExpiresInSeconds());
            
            // Use passwordEndpoint from result if available, otherwise fallback to config
            String passwordUrl = result.getPasswordEndpoint() != null 
                    ? result.getPasswordEndpoint() 
                    : authModuleBaseUrl + "/ecclesiaflow/auth/password";
            try {
                response.setPasswordEndpoint(new java.net.URI(passwordUrl));
            } catch (java.net.URISyntaxException e) {
                // Log error but continue
            }
        }
        
        return response;
    }
}
