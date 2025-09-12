package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.payloads.ConfirmationRequestPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // Rate limiting configuration for tests (1 request per period to test rate limiting)
        "resilience4j.ratelimiter.instances.confirmation-resend.limit-for-period=1",
        "resilience4j.ratelimiter.instances.confirmation-resend.limit-refresh-period=PT2S",
        "resilience4j.ratelimiter.instances.confirmation-resend.timeout-duration=PT0S",

        "resilience4j.ratelimiter.instances.confirmation-attempts.limit-for-period=1",
        "resilience4j.ratelimiter.instances.confirmation-attempts.limit-refresh-period=PT2S",
        "resilience4j.ratelimiter.instances.confirmation-attempts.timeout-duration=PT0S",

        // In-memory database configuration for tests
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("MembersConfirmationController Rate Limiting Tests")
class MembersConfirmationRateLimitingTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public MemberConfirmationService memberConfirmationService() {
            return mock(MemberConfirmationService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberConfirmationService memberConfirmationService;

    private final UUID memberId = UUID.randomUUID();

    // --- Tests for POST /ecclesiaflow/members/{memberId}/confirmation ---
    // @RateLimiter(name = "confirmation-resend")

    @Test
    @DisplayName("Should apply rate limiting on POST /confirmation")
    void confirmMember_shouldApplyRateLimiting() throws Exception {
        // Given - Use a unique UUID for this test
        UUID testMemberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        MembershipConfirmationResult serviceResult = MembershipConfirmationResult.builder()
                .message("Confirmation successful!")
                .temporaryToken("temp_token")
                .expiresInSeconds(3600)
                .build();

        when(memberConfirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenReturn(serviceResult);

        // When: Make multiple rapid calls to trigger rate limiting
        boolean rateLimitingTriggered = false;

        for (int i = 0; i < 3; i++) {
            var result = mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // If a 429 is received, rate limiting is working
            if (result.andReturn().getResponse().getStatus() == 429) {
                rateLimitingTriggered = true;
                result.andExpect(status().isTooManyRequests())
                        .andExpect(jsonPath("$.status").value(429))
                        .andExpect(jsonPath("$.error").value("Too Many Requests"));
                break;
            }
        }

        // Then: Rate limiting should have been triggered
        assertTrue(rateLimitingTriggered, "Rate limiting should be triggered after multiple rapid requests");
    }

    // --- Tests for POST /ecclesiaflow/members/{memberId}/confirmation-code ---
    // @RateLimiter(name = "confirmation-attempts")

    @Test
    @DisplayName("Should apply rate limiting on POST /confirmation-code")
    void resendConfirmationCode_shouldApplyRateLimiting() throws Exception {
        // Given - Use a unique UUID for this test
        UUID testMemberId = UUID.randomUUID();
        doNothing().when(memberConfirmationService).sendConfirmationCode(any(UUID.class));

        // When: Make multiple rapid calls to trigger rate limiting
        boolean rateLimitingTriggered = false;

        for (int i = 0; i < 3; i++) {
            var result = mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation-code", testMemberId));

            // If a 429 is received, rate limiting is working
            if (result.andReturn().getResponse().getStatus() == 429) {
                rateLimitingTriggered = true;
                result.andExpect(status().isTooManyRequests())
                        .andExpect(jsonPath("$.status").value(429))
                        .andExpect(jsonPath("$.error").value("Too Many Requests"));
                break;
            }
        }

        // Then: Rate limiting should have been triggered
        assertTrue(rateLimitingTriggered, "Rate limiting should be triggered after multiple rapid requests");
    }

    @Test
    @DisplayName("Should allow requests after the refresh period")
    void rateLimiting_shouldResetAfterRefreshPeriod() throws Exception {
        // Given - Use a unique UUID for this test
        UUID testMemberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        MembershipConfirmationResult serviceResult = MembershipConfirmationResult.builder()
                .message("Confirmation successful!")
                .temporaryToken("temp_token")
                .expiresInSeconds(3600)
                .build();

        when(memberConfirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenReturn(serviceResult);

        // When: Trigger rate limiting
        boolean rateLimitingTriggered = false;
        for (int i = 0; i < 3; i++) {
            var result = mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            if (result.andReturn().getResponse().getStatus() == 429) {
                rateLimitingTriggered = true;
                break;
            }
        }

        assertTrue(rateLimitingTriggered, "Rate limiting should be triggered");

        // Wait for the refresh period (2 seconds + margin)
        Thread.sleep(3000);

        // Then: After the refresh period, the request should pass again
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Confirmation successful!"));
    }
}