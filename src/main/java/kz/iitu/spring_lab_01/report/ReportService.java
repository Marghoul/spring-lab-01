package kz.iitu.spring_lab_01.report;

import kz.iitu.spring_lab_01.security.AppUser;
import kz.iitu.spring_lab_01.security.AppUserRepository;
import kz.iitu.spring_lab_01.security.AuthAuditEvent;
import kz.iitu.spring_lab_01.security.AuthAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lab 12, variant 10: builds login-activity reports for a period of days
 * from the auth_audit table written by the authentication event listener.
 */
@Service
public class ReportService {

    /** A report may cover at most one (leap) year, both ends inclusive. */
    public static final int MAX_PERIOD_DAYS = 366;

    private final AuthAuditRepository audit;
    private final AppUserRepository users;

    public ReportService(AuthAuditRepository audit, AppUserRepository users) {
        this.audit = audit;
        this.users = users;
    }

    /** All authentication events in [from, to], both days inclusive. */
    @Transactional(readOnly = true)
    public LoginReport loginReport(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        List<AuthAuditEvent> events = audit.findByCreatedAtBetweenOrderByCreatedAtAsc(startOf(from), endOf(to));
        return summarize(from, to, events);
    }

    /** The same report restricted to one existing user. */
    @Transactional(readOnly = true)
    public LoginReport userReport(String username, LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        List<AuthAuditEvent> events = audit.findByUsernameAndCreatedAtBetweenOrderByCreatedAtAsc(
                user.getUsername(), startOf(from), endOf(to));
        return summarize(from, to, events);
    }

    static void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidReportPeriodException("Both 'from' and 'to' are required");
        }
        if (to.isBefore(from)) {
            throw new InvalidReportPeriodException("'to' (" + to + ") is before 'from' (" + from + ")");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_PERIOD_DAYS) {
            throw new InvalidReportPeriodException(
                    "Period of " + days + " days is longer than " + MAX_PERIOD_DAYS + " days");
        }
    }

    private static LoginReport summarize(LocalDate from, LocalDate to, List<AuthAuditEvent> events) {
        if (events.isEmpty()) {
            return LoginReport.empty(from, to);   // variant 10: a period with no data
        }
        long successes = events.stream().filter(AuthAuditEvent::isSuccess).count();
        long failures = events.size() - successes;
        double rate = Math.round(successes * 1000.0 / events.size()) / 10.0;
        LoginReport.Verdict verdict = failures > successes
                ? LoginReport.Verdict.SUSPICIOUS
                : LoginReport.Verdict.NORMAL;
        return new LoginReport(from, to, events.size(), successes, failures, rate, verdict);
    }

    private static LocalDateTime startOf(LocalDate day) {
        return day.atStartOfDay();
    }

    private static LocalDateTime endOf(LocalDate day) {
        return day.atTime(LocalTime.MAX);
    }
}
