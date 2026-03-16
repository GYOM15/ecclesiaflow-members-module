package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiModelMapperTest {

    private OpenApiModelMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new OpenApiModelMapper();
        ReflectionTestUtils.setField(mapper, "authModuleBaseUrl", "http://localhost:8081");
    }

    // --- Tests for createSignUpResponse ---
    @Test
    void createSignUpResponse_shouldMapAllFieldsCorrectly() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30);
        LocalDateTime confirmedAt = LocalDateTime.of(2025, 1, 16, 14, 20);

        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .status(MemberStatus.ACTIVE)
                .createdAt(createdAt)
                .confirmedAt(confirmedAt)
                .build();

        String message = "Test message";

        // When
        SignUpResponse response = mapper.createSignUpResponse(member, message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Test message");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getAddress()).isEqualTo("123 Main St");
        assertThat(response.getConfirmed()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(createdAt.toString());
        assertThat(response.getConfirmedAt()).isEqualTo(confirmedAt.toString());
    }

    @Test
    void createSignUpResponse_shouldHandleNullMember() {
        // Given
        Member member = null;
        String message = "Test message";

        // When
        SignUpResponse response = mapper.createSignUpResponse(member, message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Test message");
        assertThat(response.getEmail()).isNull();
        assertThat(response.getFirstName()).isNull();
        assertThat(response.getLastName()).isNull();
    }

    @Test
    void createSignUpResponse_shouldHandlePendingMember() {
        // Given
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .email("unconfirmed@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .status(MemberStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .confirmedAt(null)
                .build();

        String message = "Member registered";

        // When
        SignUpResponse response = mapper.createSignUpResponse(member, message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getConfirmed()).isFalse();
        assertThat(response.getConfirmedAt()).isNull();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void createSignUpResponse_shouldHandleMemberWithoutTimestamps() {
        // Given
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .status(MemberStatus.PENDING)
                .createdAt(null)
                .confirmedAt(null)
                .build();

        String message = "Test";

        // When
        SignUpResponse response = mapper.createSignUpResponse(member, message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getConfirmedAt()).isNull();
    }

    // --- Tests for createMemberPageResponse ---
    @Test
    void createMemberPageResponse_shouldMapPageCorrectly() {
        // Given
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).email("alice@example.com").firstName("Alice").lastName("A").status(MemberStatus.ACTIVE).createdAt(LocalDateTime.now()).build(),
                Member.builder().memberId(UUID.randomUUID()).email("bob@example.com").firstName("Bob").lastName("B").status(MemberStatus.PENDING).createdAt(LocalDateTime.now()).build()
        );

        Page<Member> memberPage = new PageImpl<>(members, PageRequest.of(0, 20), 2);

        // When
        MemberPageResponse response = mapper.createMemberPageResponse(memberPage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getFirst()).isTrue();
        assertThat(response.getLast()).isTrue();
        assertThat(response.getNumberOfElements()).isEqualTo(2);
        assertThat(response.getEmpty()).isFalse();

        SignUpResponse firstMember = response.getContent().get(0);
        assertThat(firstMember.getEmail()).isEqualTo("alice@example.com");
        assertThat(firstMember.getFirstName()).isEqualTo("Alice");
        assertThat(firstMember.getConfirmed()).isTrue();

        SignUpResponse secondMember = response.getContent().get(1);
        assertThat(secondMember.getEmail()).isEqualTo("bob@example.com");
        assertThat(secondMember.getFirstName()).isEqualTo("Bob");
        assertThat(secondMember.getConfirmed()).isFalse();
    }

    @Test
    void createMemberPageResponse_shouldHandleEmptyPage() {
        // Given
        Page<Member> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        // When
        MemberPageResponse response = mapper.createMemberPageResponse(emptyPage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getEmpty()).isTrue();
    }

    @Test
    void createMemberPageResponse_shouldHandleSecondPage() {
        // Given
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).email("charlie@example.com").firstName("Charlie").status(MemberStatus.ACTIVE).createdAt(LocalDateTime.now()).build()
        );

        Page<Member> secondPage = new PageImpl<>(members, PageRequest.of(1, 20), 21);

        // When
        MemberPageResponse response = mapper.createMemberPageResponse(secondPage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNumber()).isEqualTo(1);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getFirst()).isFalse();
        assertThat(response.getLast()).isTrue();
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    // --- Tests for createConfirmationResponse ---
    @Test
    void createConfirmationResponse_shouldMapAllFieldsCorrectly() {
        // Given
        MembershipConfirmationResult result = MembershipConfirmationResult.builder()
                .message("Account confirmed successfully")
                .temporaryToken("temp-token-abc123")
                .expiresInSeconds(900)
                .build();

        // When
        ConfirmationResponse response = mapper.createConfirmationResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Account confirmed successfully");
        assertThat(response.getTemporaryToken()).isEqualTo("temp-token-abc123");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
        assertThat(response.getPasswordEndpoint()).isNotNull();
        assertThat(response.getPasswordEndpoint().toString()).isEqualTo("http://localhost:8081/ecclesiaflow/auth/password");
    }

    @Test
    void createConfirmationResponse_shouldHandleNullResult() {
        // Given
        MembershipConfirmationResult result = null;

        // When
        ConfirmationResponse response = mapper.createConfirmationResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTemporaryToken()).isNull();
        assertThat(response.getExpiresIn()).isNull();
    }

    @Test
    void createConfirmationResponse_shouldHandleDifferentAuthModuleUrl() {
        // Given
        ReflectionTestUtils.setField(mapper, "authModuleBaseUrl", "https://production.example.com");

        MembershipConfirmationResult result = MembershipConfirmationResult.builder()
                .message("Success")
                .temporaryToken("token")
                .expiresInSeconds(600)
                .build();

        // When
        ConfirmationResponse response = mapper.createConfirmationResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPasswordEndpoint()).isNotNull();
        assertThat(response.getPasswordEndpoint().toString()).isEqualTo("https://production.example.com/ecclesiaflow/auth/password");
    }
}
