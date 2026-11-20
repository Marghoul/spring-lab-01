package kz.iitu.spring_lab_01.report;

public class InvalidReportPeriodException extends RuntimeException {
    public InvalidReportPeriodException(String message) {
        super(message);
    }
}
