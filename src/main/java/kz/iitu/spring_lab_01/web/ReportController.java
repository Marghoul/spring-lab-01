package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.report.LoginReport;
import kz.iitu.spring_lab_01.report.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** GET /api/reports/logins?from=2026-11-01&to=2026-11-30[&user=admin] - administrators only. */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/logins")
    public LoginReport logins(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                              @RequestParam(required = false) String user) {
        return user == null ? reports.loginReport(from, to) : reports.userReport(user, from, to);
    }
}
