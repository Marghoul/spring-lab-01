package kz.iitu.spring_lab_01.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Task B: outer transactional methods. AuditService is a separate bean,
 * so the call goes through the Spring proxy and its propagation is honoured.
 */
@Service
public class PropagationDemoService {

    private final AccountRepository accounts;
    private final AuditService auditService;

    public PropagationDemoService(AccountRepository accounts, AuditService auditService) {
        this.accounts = accounts;
        this.auditService = auditService;
    }

    @Transactional
    public void outerWithRequired(Long accountId, String auditMessage) {
        accounts.findById(accountId).orElseThrow().credit(new BigDecimal("1.00"));
        auditService.logRequired(auditMessage);
        throw new IllegalStateException("Outer transaction fails after REQUIRED audit");
    }

    @Transactional
    public void outerWithRequiresNew(Long accountId, String auditMessage) {
        accounts.findById(accountId).orElseThrow().credit(new BigDecimal("1.00"));
        auditService.logRequiresNew(auditMessage);
        throw new IllegalStateException("Outer transaction fails after REQUIRES_NEW audit");
    }
}
