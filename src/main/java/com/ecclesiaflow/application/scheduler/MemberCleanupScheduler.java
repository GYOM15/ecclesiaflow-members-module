package com.ecclesiaflow.application.scheduler;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled task that anonymizes DEACTIVATED members after the grace period.
 *
 * <p>Runs daily at 02:00. For each expired member:
 * deletes the Keycloak user, scrubs PII, and sets status to INACTIVE.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;
    private final AuthClient authClient;

    @Value("${ecclesiaflow.members.deactivation.grace-period-days:30}")
    private int gracePeriodDays;

    @Scheduled(cron = "0 0 2 * * ?")
    public void anonymizeExpiredDeactivatedMembers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(gracePeriodDays);
        List<Member> expired = memberRepository.findDeactivatedBefore(cutoff);

        if (expired.isEmpty()) {
            log.info("Member cleanup: no expired deactivated members found");
            return;
        }

        log.info("Member cleanup: processing {} expired deactivated member(s)", expired.size());
        int success = 0;
        int failures = 0;

        for (Member member : expired) {
            try {
                anonymizeMember(member);
                success++;
            } catch (Exception e) {
                failures++;
                log.error("Member cleanup: failed to anonymize member (id={})", member.getId(), e);
            }
        }

        log.info("Member cleanup: done — {} anonymized, {} failed", success, failures);
    }

    @Transactional
    protected void anonymizeMember(Member member) {
        if (member.getKeycloakUserId() != null) {
            authClient.deleteKeycloakUser(member.getKeycloakUserId());
        }

        Member anonymized = member.toBuilder()
                .firstName("DELETED")
                .lastName("DELETED")
                .email("deleted+" + UUID.randomUUID() + "@anonymized.local")
                .address(null)
                .phoneNumber(null)
                .keycloakUserId(null)
                .status(MemberStatus.INACTIVE)
                .anonymizedAt(LocalDateTime.now())
                .build();

        memberRepository.save(anonymized);
    }
}
