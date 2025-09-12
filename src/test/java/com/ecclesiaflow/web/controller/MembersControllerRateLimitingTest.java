package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.payloads.SignUpRequestPayload;
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
    // Configuration rate limiting for tests (1 request per période to test rate limiting)
    "resilience4j.ratelimiter.instances.member-registration.limit-for-period=1",
    "resilience4j.ratelimiter.instances.member-registration.limit-refresh-period=PT2S",
    "resilience4j.ratelimiter.instances.member-registration.timeout-duration=PT0S",
    
    // Database configuration for tests
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("MembersController Rate Limiting Tests")
class MembersControllerRateLimitingTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public MemberService memberService() {
            return mock(MemberService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    // --- Tests for POST /ecclesiaflow/members ---
    // @RateLimiter(name = "member-registration")

    @Test
    @DisplayName("Devrait appliquer le rate limiting sur POST /members (registerMember)")
    void registerMember_shouldApplyRateLimiting() throws Exception {
        // Given
        SignUpRequestPayload request = new SignUpRequestPayload();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setAddress("123 Main Street");
        request.setPhoneNumber("+1234567890");

        Member mockMember = Member.builder()
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main Street")
                .phoneNumber("+1234567890")
                .build();

        when(memberService.registerMember(any(MembershipRegistration.class)))
                .thenReturn(mockMember);

        // When: Make multiple requests to trigger rate limiting
        boolean rateLimitingTriggered = false;
        
        for (int i = 0; i < 3; i++) {
            var result = mockMvc.perform(post("/ecclesiaflow/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
            
            // When we get a 429, the rate limiting is triggered
            if (result.andReturn().getResponse().getStatus() == 429) {
                rateLimitingTriggered = true;
                result.andExpect(status().isTooManyRequests())
                      .andExpect(jsonPath("$.status").value(429))
                      .andExpect(jsonPath("$.error").value("Too Many Requests"));
                break;
            }
        }

        // Then: The rate limiting should be triggered
        assertTrue(rateLimitingTriggered, "Le rate limiting devrait être déclenché après plusieurs requêtes rapides");
    }

    @Test
    @DisplayName("Devrait permettre les requêtes après la période de refresh")
    void memberRegistration_shouldResetAfterRefreshPeriod() throws Exception {
        // Given
        SignUpRequestPayload request = new SignUpRequestPayload();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");
        request.setAddress("456 Oak Avenue");
        request.setPhoneNumber("+1987654321");

        Member mockMember = Member.builder()
                .email("jane.smith@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .address("456 Oak Avenue")
                .phoneNumber("+1987654321")
                .build();

        when(memberService.registerMember(any(MembershipRegistration.class)))
                .thenReturn(mockMember);

        // When: Trigger the rate limiting
        boolean rateLimitingTriggered = false;
        for (int i = 0; i < 3; i++) {
            var result = mockMvc.perform(post("/ecclesiaflow/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
            
            if (result.andReturn().getResponse().getStatus() == 429) {
                rateLimitingTriggered = true;
                break;
            }
        }

        assertTrue(rateLimitingTriggered, "Le rate limiting devrait être déclenché");

        // Wait for the refresh period to pass (2 seconds + margin)
        Thread.sleep(3000);

        // Then: After the refresh period, the request should pass again
        mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Member registered (temporary - approval system coming)"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    @DisplayName("Devrait appliquer le rate limiting indépendamment des données de la requête")
    void registerMember_shouldApplyRateLimitingRegardlessOfRequestData() throws Exception {
        // Given - Two different requests
        SignUpRequestPayload request1 = new SignUpRequestPayload();
        request1.setFirstName("Alice");
        request1.setLastName("Johnson");
        request1.setEmail("alice.johnson@example.com");
        request1.setAddress("789 Pine Street");
        request1.setPhoneNumber("+1122334455");

        SignUpRequestPayload request2 = new SignUpRequestPayload();
        request2.setFirstName("Bob");
        request2.setLastName("Wilson");
        request2.setEmail("bob.wilson@example.com");
        request2.setAddress("321 Elm Drive");
        request2.setPhoneNumber("+1555666777");

        Member mockMember = Member.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .address("Test Address")
                .phoneNumber("+1000000000")
                .build();

        when(memberService.registerMember(any(MembershipRegistration.class)))
                .thenReturn(mockMember);

        // When: Make calls with different data
        boolean rateLimitingTriggered = false;
        
        // First call with request1
        var result1 = mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)));
        
        // Second call with request2 (different data)
        var result2 = mockMvc.perform(post("/ecclesiaflow/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)));
        
        // The rate limiting should be triggered even if the data is different
        if (result2.andReturn().getResponse().getStatus() == 429) {
            rateLimitingTriggered = true;
            result2.andExpect(status().isTooManyRequests())
                   .andExpect(jsonPath("$.status").value(429))
                   .andExpect(jsonPath("$.error").value("Too Many Requests"));
        }

        // Then: The rate limiting should be triggered
        assertTrue(rateLimitingTriggered, "Le rate limiting devrait s'appliquer indépendamment des données de la requête");
    }
}
