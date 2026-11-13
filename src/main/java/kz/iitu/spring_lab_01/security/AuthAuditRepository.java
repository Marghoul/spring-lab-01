package kz.iitu.spring_lab_01.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthAuditRepository extends JpaRepository<AuthAuditEvent, Long> {
    List<AuthAuditEvent> findTop20ByOrderByIdDesc();
}
