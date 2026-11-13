package kz.iitu.spring_lab_01.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Individual assignment, variant 10: listens for authentication success and failure
 * events published by the ProviderManager and writes an audit row for each one.
 * The password is never written: only the username, the event type and the reason.
 */
@Component
public class AuthenticationAuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationAuditListener.class);

    private final AuthAuditRepository audit;

    public AuthenticationAuditListener(AuthAuditRepository audit) {
        this.audit = audit;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        save(new AuthAuditEvent(auth.getName(), event.getClass().getSimpleName(), true,
                null, remoteAddr(auth)));
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        save(new AuthAuditEvent(auth.getName(), event.getClass().getSimpleName(), false,
                event.getException().getClass().getSimpleName(), // class only: messages are localized
                remoteAddr(auth)));
    }

    private void save(AuthAuditEvent row) {
        audit.save(row);
        log.info("AUTH AUDIT user={} event={} success={} reason={} ip={}",
                row.getUsername(), row.getEventType(), row.isSuccess(), row.getReason(), row.getRemoteAddr());
    }

    private static String remoteAddr(Authentication auth) {
        return auth.getDetails() instanceof WebAuthenticationDetails d ? d.getRemoteAddress() : null;
    }
}
