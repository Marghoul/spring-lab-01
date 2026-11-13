package kz.iitu.spring_lab_01.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Task B: inner methods that write an audit record with different propagation. */
@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /** Joins the caller transaction (the default). */
    @Transactional(propagation = Propagation.REQUIRED)
    public void logRequired(String message) {
        repository.save(new AuditLog(message));
    }

    /** Suspends the caller transaction and commits in its own physical transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequiresNew(String message) {
        repository.save(new AuditLog(message));
    }
}
