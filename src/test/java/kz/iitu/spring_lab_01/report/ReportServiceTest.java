package kz.iitu.spring_lab_01.report;

import kz.iitu.spring_lab_01.security.AppUserRepository;
import kz.iitu.spring_lab_01.security.AuthAuditEvent;
import kz.iitu.spring_lab_01.security.AuthAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Task 1: unit tests of ReportService (variant 10). No Spring context: the two
 * repositories are Mockito mocks and the service is created by @InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_30 = LocalDate.of(2026, 11, 30);

    @Mock AuthAuditRepository audit;
    @Mock AppUserRepository users;
    @InjectMocks ReportService service;

    private static AuthAuditEvent event(String user, boolean success) {
        return new AuthAuditEvent(user, success ? "AuthenticationSuccessEvent"
                : "AuthenticationFailureBadCredentialsEvent", success, null, "127.0.0.1",
                LocalDateTime.of(2026, 11, 10, 12, 0));
    }

    // 1. happy path
    @Test
    void reportCountsSuccessesAndFailuresOfThePeriod() {
        given(audit.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).willReturn(List.of(
                event("user", true), event("admin", true), event("user", false), event("admin", true)));

        LoginReport report = service.loginReport(NOV_1, NOV_30);

        assertThat(report.total()).isEqualTo(4);
        assertThat(report.successes()).isEqualTo(3);
        assertThat(report.failures()).isEqualTo(1);
        assertThat(report.successRate()).isEqualTo(75.0);
        assertThat(report.verdict()).isEqualTo(LoginReport.Verdict.NORMAL);
    }

    // 2. not-found path
    @Test
    void reportForAnUnknownUserIsRejectedWithoutReadingTheAudit() {
        given(users.findByUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.userReport("ghost", NOV_1, NOV_30))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User 'ghost' not found");
        verifyNoInteractions(audit);
    }

    // 3. interaction check with an ArgumentCaptor
    @Test
    void periodIsTranslatedIntoInclusiveStartAndEndTimestamps() {
        given(audit.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).willReturn(List.of());

        service.loginReport(NOV_1, NOV_30);

        ArgumentCaptor<LocalDateTime> from = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(audit).findByCreatedAtBetweenOrderByCreatedAtAsc(from.capture(), to.capture());
        assertThat(from.getValue()).isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
        // the whole last day belongs to the period, up to the last nanosecond
        assertThat(to.getValue()).isEqualTo(LocalDateTime.of(2026, 11, 30, 23, 59, 59, 999_999_999));
    }

    // 4. the variant branch: a report for a period with no data
    @Test
    void periodWithNoDataGivesAnEmptyReportInsteadOfDividingByZero() {
        given(audit.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).willReturn(List.of());

        LoginReport report = service.loginReport(NOV_1, NOV_30);

        assertThat(report.total()).isZero();
        assertThat(report.successes()).isZero();
        assertThat(report.failures()).isZero();
        assertThat(report.successRate()).isZero().isNotNaN();
        assertThat(report.verdict()).isEqualTo(LoginReport.Verdict.NO_DATA);
        assertThat(report.from()).isEqualTo(NOV_1);
        assertThat(report.to()).isEqualTo(NOV_30);
    }

    // Task 5: the test written after reading the JaCoCo report - line 74 was yellow
    // (only the "failures <= successes" way through the condition had been taken)
    @Test
    void moreFailuresThanSuccessesMarksThePeriodAsSuspicious() {
        given(audit.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).willReturn(List.of(
                event("admin", false), event("admin", false), event("admin", true)));

        LoginReport report = service.loginReport(NOV_1, NOV_30);

        assertThat(report.failures()).isEqualTo(2);
        assertThat(report.successRate()).isEqualTo(33.3);
        assertThat(report.verdict()).isEqualTo(LoginReport.Verdict.SUSPICIOUS);
    }

    // variant extra requirement: @CsvSource covering the boundary values of the period
    @ParameterizedTest(name = "[{index}] {4}: {0} .. {1} = {2} day(s) -> valid={3}")
    @CsvSource(textBlock = """
            # from,      to,          days, valid, case
            2026-11-20, 2026-11-20,   1,    true,  shortest period - one day
            2026-11-20, 2026-11-19,   0,    false, end one day before start
            2026-01-01, 2026-12-31,   365,  true,  an ordinary year
            2024-01-01, 2024-12-31,   366,  true,  exactly MAX_PERIOD_DAYS (leap year)
            2024-01-01, 2025-01-01,   367,  false, MAX_PERIOD_DAYS + 1
            """)
    void periodLengthIsCheckedAtItsBoundaries(LocalDate from, LocalDate to, int days, boolean valid,
                                              String description) {
        assertThat(ChronoUnit.DAYS.between(from, to) + 1).as("days in " + description).isEqualTo(days);
        if (valid) {
            given(audit.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).willReturn(List.of());
            assertThat(service.loginReport(from, to).verdict()).isEqualTo(LoginReport.Verdict.NO_DATA);
        } else {
            assertThatThrownBy(() -> service.loginReport(from, to))
                    .isInstanceOf(InvalidReportPeriodException.class);
            verifyNoInteractions(audit);
        }
    }
}
