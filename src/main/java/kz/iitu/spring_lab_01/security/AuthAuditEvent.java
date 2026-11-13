package kz.iitu.spring_lab_01.security;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Variant 10: one row per authentication success or failure. */
@Entity
@Table(name = "auth_audit")
public class AuthAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 200)
    private String reason;

    @Column(name = "remote_addr", length = 64)
    private String remoteAddr;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AuthAuditEvent() {
    }

    public AuthAuditEvent(String username, String eventType, boolean success, String reason, String remoteAddr) {
        this.username = username;
        this.eventType = eventType;
        this.success = success;
        this.reason = reason;
        this.remoteAddr = remoteAddr;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEventType() { return eventType; }
    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
    public String getRemoteAddr() { return remoteAddr; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
