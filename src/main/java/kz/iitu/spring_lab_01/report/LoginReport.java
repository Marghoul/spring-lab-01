package kz.iitu.spring_lab_01.report;

import java.time.LocalDate;

/**
 * Summary of authentication events (variant 10 of lab 8) for a period.
 *
 * @param successRate percentage of successful logins, 0..100; 0 when there was no data
 */
public record LoginReport(LocalDate from, LocalDate to,
                          long total, long successes, long failures,
                          double successRate, Verdict verdict) {

    public enum Verdict {
        /** the period contains no authentication events at all */
        NO_DATA,
        /** failures do not outnumber successes */
        NORMAL,
        /** more failed logins than successful ones - possible password guessing */
        SUSPICIOUS
    }

    static LoginReport empty(LocalDate from, LocalDate to) {
        return new LoginReport(from, to, 0, 0, 0, 0.0, Verdict.NO_DATA);
    }
}
