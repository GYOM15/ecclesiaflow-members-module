package com.ecclesiaflow.application.scheduler;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCleanupSchedulerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private MemberCleanupScheduler scheduler;

    private Member buildDeactivatedMember(String keycloakUserId) {
        return Member.builder()
                .id(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .address("123 Main St")
                .phoneNumber("+1234567890")
                .keycloakUserId(keycloakUserId)
                .status(MemberStatus.DEACTIVATED)
                .deactivatedAt(LocalDateTime.now().minusDays(31))
                .build();
    }

    @Test
    void shouldAnonymizePiiFields() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);
        Member member = buildDeactivatedMember("kc-123");

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.anonymizeExpiredDeactivatedMembers();

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());

        Member anonymized = captor.getValue();
        assertThat(anonymized.getFirstName()).isEqualTo("DELETED");
        assertThat(anonymized.getLastName()).isEqualTo("DELETED");
        assertThat(anonymized.getEmail()).startsWith("deleted+").endsWith("@anonymized.local");
        assertThat(anonymized.getAddress()).isNull();
        assertThat(anonymized.getPhoneNumber()).isNull();
        assertThat(anonymized.getKeycloakUserId()).isNull();
        assertThat(anonymized.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        assertThat(anonymized.getAnonymizedAt()).isNotNull();
    }

    @Test
    void shouldDeleteKeycloakUserBeforeAnonymizing() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);
        String keycloakUserId = "kc-456";
        Member member = buildDeactivatedMember(keycloakUserId);

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.anonymizeExpiredDeactivatedMembers();

        var inOrder = inOrder(authClient, memberRepository);
        inOrder.verify(authClient).deleteKeycloakUser(keycloakUserId);
        inOrder.verify(memberRepository).save(any(Member.class));
    }

    @Test
    void shouldSkipKeycloakDeletionWhenUserIdIsNull() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);
        Member member = buildDeactivatedMember(null);

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.anonymizeExpiredDeactivatedMembers();

        verify(authClient, never()).deleteKeycloakUser(any());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void shouldDoNothingWhenNoExpiredMembers() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduler.anonymizeExpiredDeactivatedMembers();

        verify(authClient, never()).deleteKeycloakUser(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void shouldContinueProcessingOnFailure() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);
        Member member1 = buildDeactivatedMember("kc-fail");
        Member member2 = buildDeactivatedMember("kc-success");

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(member1, member2));
        doThrow(new RuntimeException("Keycloak down"))
                .when(authClient).deleteKeycloakUser("kc-fail");
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.anonymizeExpiredDeactivatedMembers();

        // Second member should still be processed despite first failure
        verify(authClient).deleteKeycloakUser("kc-success");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void shouldSetStatusToInactive() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);
        Member member = buildDeactivatedMember("kc-789");

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.anonymizeExpiredDeactivatedMembers();

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(MemberStatus.INACTIVE);
    }

    @Test
    void shouldProcessMultipleMembers() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 30);
        Member member1 = buildDeactivatedMember("kc-1");
        Member member2 = buildDeactivatedMember("kc-2");
        Member member3 = buildDeactivatedMember("kc-3");

        when(memberRepository.findDeactivatedBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(member1, member2, member3));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.anonymizeExpiredDeactivatedMembers();

        verify(authClient, times(3)).deleteKeycloakUser(any());
        verify(memberRepository, times(3)).save(any(Member.class));
    }
}
