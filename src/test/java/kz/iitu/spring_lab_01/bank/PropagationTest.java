package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

/** Task B: REQUIRED vs REQUIRES_NEW. */
class PropagationTest extends AbstractBankTest {

    @Autowired PropagationDemoService demo;
    @Autowired AuditLogRepository auditLogs;

    @Test
    void requiredAuditIsRolledBackWithOuterTransaction() {
        assertThatThrownBy(() -> demo.outerWithRequired(ALICE, "audit-REQUIRED"))
                .isInstanceOf(IllegalStateException.class);

        boolean kept = auditLogs.existsByMessage("audit-REQUIRED");
        System.out.printf("[TASK B] REQUIRED: audit kept=%s, Alice=%s%n", kept, balanceOf(ALICE));
        assertThat(kept).isFalse();
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1000.00");
    }

    @Test
    void requiresNewAuditSurvivesOuterRollback() {
        assertThatThrownBy(() -> demo.outerWithRequiresNew(ALICE, "audit-REQUIRES_NEW"))
                .isInstanceOf(IllegalStateException.class);

        boolean kept = auditLogs.existsByMessage("audit-REQUIRES_NEW");
        System.out.printf("[TASK B] REQUIRES_NEW: audit kept=%s, Alice=%s%n", kept, balanceOf(ALICE));
        assertThat(kept).isTrue();
        // the outer business change is still rolled back
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1000.00");
    }
}
