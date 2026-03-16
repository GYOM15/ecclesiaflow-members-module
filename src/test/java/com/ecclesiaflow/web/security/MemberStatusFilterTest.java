package com.ecclesiaflow.web.security;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MemberStatusFilter.
 * Verifies that deactivated, suspended, and inactive members are blocked
 * while active, confirmed, pending, and unknown members pass through.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberStatusFilter - Unit Tests")
class MemberStatusFilterTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FilterChain filterChain;

    private MemberStatusFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        filter = new MemberStatusFilter(memberRepository, objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    // === PASS-THROUGH TESTS ===

    @Test
    @DisplayName("Should pass through when no authentication is present")
    void doFilter_WhenUnauthenticated_ShouldPassThrough() throws ServletException, IOException {
        request.setRequestURI("/ecclesiaflow/members/me");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should pass through when member is ACTIVE")
    void doFilter_WhenMemberIsActive_ShouldPassThrough() throws ServletException, IOException {
        String keycloakUserId = "kc-123";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        Member member = Member.builder()
            .memberId(UUID.randomUUID())
            .status(MemberStatus.ACTIVE)
            .keycloakUserId(keycloakUserId)
            .build();
        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(member));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should pass through when member is CONFIRMED")
    void doFilter_WhenMemberIsConfirmed_ShouldPassThrough() throws ServletException, IOException {
        String keycloakUserId = "kc-123";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        Member member = Member.builder()
            .memberId(UUID.randomUUID())
            .status(MemberStatus.CONFIRMED)
            .keycloakUserId(keycloakUserId)
            .build();
        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(member));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should pass through when member is PENDING")
    void doFilter_WhenMemberIsPending_ShouldPassThrough() throws ServletException, IOException {
        String keycloakUserId = "kc-123";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        Member member = Member.builder()
            .memberId(UUID.randomUUID())
            .status(MemberStatus.PENDING)
            .keycloakUserId(keycloakUserId)
            .build();
        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(member));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should pass through when member not found (SSO onboarding)")
    void doFilter_WhenMemberNotFound_ShouldPassThrough() throws ServletException, IOException {
        String keycloakUserId = "kc-new-user";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // === BLOCKED STATUS TESTS ===

    @Test
    @DisplayName("Should block DEACTIVATED member with 403 and ACCOUNT_DEACTIVATED errorCode")
    void doFilter_WhenMemberIsDeactivated_ShouldReturn403() throws ServletException, IOException {
        String keycloakUserId = "kc-deactivated";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        Member member = Member.builder()
            .memberId(UUID.randomUUID())
            .status(MemberStatus.DEACTIVATED)
            .keycloakUserId(keycloakUserId)
            .build();
        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(member));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");

        String body = response.getContentAsString();
        assertThat(body).contains("\"errorCode\":\"ACCOUNT_DEACTIVATED\"");
        assertThat(body).contains("\"status\":403");
        assertThat(body).contains("\"error\":\"Forbidden\"");
        assertThat(body).contains("\"path\":\"/ecclesiaflow/members/me\"");
    }

    @Test
    @DisplayName("Should block SUSPENDED member with 403 and ACCOUNT_SUSPENDED errorCode")
    void doFilter_WhenMemberIsSuspended_ShouldReturn403() throws ServletException, IOException {
        String keycloakUserId = "kc-suspended";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        Member member = Member.builder()
            .memberId(UUID.randomUUID())
            .status(MemberStatus.SUSPENDED)
            .keycloakUserId(keycloakUserId)
            .build();
        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(member));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(403);
        String body = response.getContentAsString();
        assertThat(body).contains("\"errorCode\":\"ACCOUNT_SUSPENDED\"");
    }

    @Test
    @DisplayName("Should block INACTIVE member with 403 and ACCOUNT_INACTIVE errorCode")
    void doFilter_WhenMemberIsInactive_ShouldReturn403() throws ServletException, IOException {
        String keycloakUserId = "kc-inactive";
        setUpJwtAuthentication(keycloakUserId);
        request.setRequestURI("/ecclesiaflow/members/me");

        Member member = Member.builder()
            .memberId(UUID.randomUUID())
            .status(MemberStatus.INACTIVE)
            .keycloakUserId(keycloakUserId)
            .build();
        when(memberRepository.getByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(member));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(403);
        String body = response.getContentAsString();
        assertThat(body).contains("\"errorCode\":\"ACCOUNT_INACTIVE\"");
    }

    // === SHOULDNOTFILTER TESTS ===

    @ParameterizedTest
    @ValueSource(strings = {
        "/ecclesiaflow/members",
        "/ecclesiaflow/members/confirmation",
        "/ecclesiaflow/members/new-confirmation",
        "/ecclesiaflow/members/me/email/confirm",
        "/ecclesiaflow/members/me/reactivate",
        "/actuator/health",
        "/actuator/health/liveness",
        "/actuator/health/readiness"
    })
    @DisplayName("Should not filter public and excluded endpoints")
    void shouldNotFilter_ForExcludedPaths(String path) {
        request.setRequestURI(path);

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/ecclesiaflow/members/me",
        "/ecclesiaflow/members/123",
        "/ecclesiaflow/members/me/email"
    })
    @DisplayName("Should filter protected endpoints")
    void shouldNotFilter_ForProtectedPaths(String path) {
        request.setRequestURI(path);

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    // === HELPERS ===

    private void setUpJwtAuthentication(String subject) {
        Jwt jwt = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(300),
            Map.of("alg", "RS256"),
            Map.of("sub", subject)
        );
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
