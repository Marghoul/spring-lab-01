package kz.iitu.spring_lab_01.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task 3: data slice for the derived queries that ReportService (variant 10) uses.
 * @DataJpaTest uses an embedded H2 database (stated explicitly: this is NOT the
 * integration test) on which Flyway runs V1..V7; every test is rolled back.
 */
@DataJpaTest
class AuthAuditRepositoryTest {

    private static final LocalDateTime NOV_1 = LocalDateTime.of(2026, 11, 1, 0, 0);
    private static final LocalDateTime NOV_30_END = LocalDateTime.of(2026, 11, 30, 23, 59, 59, 999_999_999);

    @Autowired AuthAuditRepository repository;
    @Autowired TestEntityManager em;

    private void persist(String user, boolean success, LocalDateTime at) {
        em.persist(new AuthAuditEvent(user, success ? "AuthenticationSuccessEvent"
                : "AuthenticationFailureBadCredentialsEvent", success, null, "127.0.0.1", at));
    }

    @BeforeEach
    void rows() {
        persist("user",  true,  LocalDateTime.of(2026, 10, 31, 23, 59, 59)); // 1 s before the period
        persist("user",  true,  LocalDateTime.of(2026, 11, 1, 0, 0));        // first instant
        persist("admin", false, LocalDateTime.of(2026, 11, 15, 12, 30));
        persist("user",  false, LocalDateTime.of(2026, 11, 30, 23, 59, 59)); // last second
        persist("admin", true,  LocalDateTime.of(2026, 12, 1, 0, 0));        // first instant after
        em.flush();
        em.clear();          // force the query to hit the database
    }

    @Test
    void findsEveryEventInsideThePeriodInChronologicalOrder() {
        var found = repository.findByCreatedAtBetweenOrderByCreatedAtAsc(NOV_1, NOV_30_END);

        assertThat(found).extracting(AuthAuditEvent::getCreatedAt).containsExactly(
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 11, 15, 12, 30),
                LocalDateTime.of(2026, 11, 30, 23, 59, 59));
    }

    @Test
    void excludesOtherUsersAndEventsJustOutsideThePeriod() {
        var found = repository.findByUsernameAndCreatedAtBetweenOrderByCreatedAtAsc("user", NOV_1, NOV_30_END);

        assertThat(found).extracting(AuthAuditEvent::getUsername).containsOnly("user");
        assertThat(found).extracting(AuthAuditEvent::getCreatedAt)
                .doesNotContain(LocalDateTime.of(2026, 10, 31, 23, 59, 59))   // before
                .doesNotContain(LocalDateTime.of(2026, 11, 15, 12, 30))       // admin's row
                .hasSize(2);
    }

    @Test
    void aPeriodWithoutEventsReturnsAnEmptyList() {
        var found = repository.findByCreatedAtBetweenOrderByCreatedAtAsc(
                LocalDateTime.of(2026, 6, 1, 0, 0), LocalDateTime.of(2026, 6, 30, 23, 59, 59));

        assertThat(found).isEmpty();
    }
}
