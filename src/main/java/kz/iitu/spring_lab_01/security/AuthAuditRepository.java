package kz.iitu.spring_lab_01.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuthAuditRepository extends JpaRepository<AuthAuditEvent, Long> {

    List<AuthAuditEvent> findTop20ByOrderByIdDesc();

    /** Used by ReportService: events whose created_at lies in [from, to] (both inclusive). */
    List<AuthAuditEvent> findByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime from, LocalDateTime to);

    List<AuthAuditEvent> findByUsernameAndCreatedAtBetweenOrderByCreatedAtAsc(
            String username, LocalDateTime from, LocalDateTime to);
}
