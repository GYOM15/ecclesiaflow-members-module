package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.web.dto.MemberPageResponse;
import com.ecclesiaflow.web.dto.SignUpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberPageMapper Tests")
class MemberPageMapperTest {

    @InjectMocks
    private MemberPageMapper memberPageMapper;

    private Member testMember1;
    private Member testMember2;
    private SignUpResponse testResponse1;
    private SignUpResponse testResponse2;

    @BeforeEach
    void setUp() {
        testMember1 = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .address("123 Rue Test")
                .confirmed(true)
                .createdAt(LocalDateTime.now())
                .build();

        testMember2 = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("Marie")
                .lastName("Martin")
                .email("marie.martin@example.com")
                .address("456 Avenue Test")
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .build();

        testResponse1 = SignUpResponse.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .address("123 Rue Test")
                .confirmed(true)
                .message("Membre récupéré")
                .build();

        testResponse2 = SignUpResponse.builder()
                .firstName("Marie")
                .lastName("Martin")
                .email("marie.martin@example.com")
                .address("456 Avenue Test")
                .confirmed(false)
                .message("Membre récupéré")
                .build();
    }

    @Test
    @DisplayName("Should convert Page<Member> to MemberPageResponse successfully")
    void shouldConvertPageToResponseSuccessfully() {
        // Given
        List<Member> members = List.of(testMember1, testMember2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> memberPage = new PageImpl<>(members, pageable, 25); // 25 total elements

        try (MockedStatic<MemberResponseMapper> mockedMapper = mockStatic(MemberResponseMapper.class)) {
            mockedMapper.when(() -> MemberResponseMapper.fromMember(testMember1, "Membre récupéré"))
                    .thenReturn(testResponse1);
            mockedMapper.when(() -> MemberResponseMapper.fromMember(testMember2, "Membre récupéré"))
                    .thenReturn(testResponse2);

            // When
            MemberPageResponse result = memberPageMapper.toPageResponse(memberPage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(testResponse1, testResponse2);

            // Test pagination metadata
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getNumber()).isEqualTo(0); // Alias for page
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(25);
            assertThat(result.getTotalPages()).isEqualTo(3); // 25 / 10 = 3 pages
            assertThat(result.getNumberOfElements()).isEqualTo(2); // Current page has 2 elements
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Test
    @DisplayName("Should handle empty page correctly")
    void shouldHandleEmptyPageCorrectly() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        // When
        MemberPageResponse result = memberPageMapper.toPageResponse(emptyPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("Should handle last page correctly")
    void shouldHandleLastPageCorrectly() {
        // Given - Page 2 of 3 (last page with fewer elements)
        List<Member> members = List.of(testMember1); // Only 1 element on last page
        Pageable pageable = PageRequest.of(2, 10);
        Page<Member> lastPage = new PageImpl<>(members, pageable, 21); // 21 total = 3 pages

        try (MockedStatic<MemberResponseMapper> mockedMapper = mockStatic(MemberResponseMapper.class)) {
            mockedMapper.when(() -> MemberResponseMapper.fromMember(any(Member.class), eq("Membre récupéré")))
                    .thenReturn(testResponse1);

            // When
            MemberPageResponse result = memberPageMapper.toPageResponse(lastPage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPage()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(21);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumberOfElements()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when page is null")
    void shouldThrowExceptionWhenPageIsNull() {
        // When & Then
        assertThatThrownBy(() -> memberPageMapper.toPageResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member page cannot be null");
    }

    @Test
    @DisplayName("Should handle single element page")
    void shouldHandleSingleElementPage() {
        // Given
        List<Member> members = List.of(testMember1);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Member> singleElementPage = new PageImpl<>(members, pageable, 1);

        try (MockedStatic<MemberResponseMapper> mockedMapper = mockStatic(MemberResponseMapper.class)) {
            mockedMapper.when(() -> MemberResponseMapper.fromMember(testMember1, "Membre récupéré"))
                    .thenReturn(testResponse1);

            // When
            MemberPageResponse result = memberPageMapper.toPageResponse(singleElementPage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testResponse1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }
    }

    @Test
    @DisplayName("Should preserve all Spring Data Page properties")
    void shouldPreserveAllSpringDataPageProperties() {
        // Given - Middle page to test all properties
        List<Member> members = List.of(testMember1, testMember2);
        Pageable pageable = PageRequest.of(1, 2); // Page 1, size 2
        Page<Member> middlePage = new PageImpl<>(members, pageable, 10); // 10 total = 5 pages

        try (MockedStatic<MemberResponseMapper> mockedMapper = mockStatic(MemberResponseMapper.class)) {
            mockedMapper.when(() -> MemberResponseMapper.fromMember(any(Member.class), eq("Membre récupéré")))
                    .thenReturn(testResponse1, testResponse2);

            // When
            MemberPageResponse result = memberPageMapper.toPageResponse(middlePage);

            // Then - Verify all properties are correctly mapped
            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getNumber()).isEqualTo(1); // Should be same as page
            assertThat(result.getSize()).isEqualTo(2);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse(); // Not first page
            assertThat(result.isLast()).isFalse();  // Not last page
            assertThat(result.isEmpty()).isFalse(); // Has content
        }
    }
}