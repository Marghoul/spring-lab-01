package kz.iitu.spring_lab_01.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Task A: declarative transactions and rollback rules. */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository repository;

    public TransferService(AccountRepository repository) {
        this.repository = repository;
    }

    /** Normal atomic transfer. Any RuntimeException rolls back both updates. */
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();
        from.debit(amount);
        to.credit(amount);
        repository.save(from);
        repository.save(to);
        log.info("Transferred {} from {} to {}", amount, from.getOwnerName(), to.getOwnerName());
    }

    /** Unchecked exception after the debit, before the credit: full rollback. */
    @Transactional
    public void transferFailingAfterDebit(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from); // the UPDATE is already sent to the database
        log.info("Debited {} from {}, now simulating a crash before crediting {}",
                amount, from.getOwnerName(), to.getOwnerName());
        simulateCrash();
        to.credit(amount);
        repository.save(to);
    }

    /** Checked exception with default rules: the transaction COMMITS (debit is kept). */
    @Transactional
    public void transferFailingWithCheckedException(Long fromId, Long toId, BigDecimal amount)
            throws BusinessException {
        Account from = repository.findById(fromId).orElseThrow();
        repository.findById(toId).orElseThrow();
        from.debit(amount);
        throw new BusinessException("Checked failure after debit (default rules)");
    }

    /** Checked exception with rollbackFor: the transaction ROLLS BACK. */
    @Transactional(rollbackFor = BusinessException.class)
    public void transferFailingWithCheckedExceptionRollbackFor(Long fromId, Long toId, BigDecimal amount)
            throws BusinessException {
        Account from = repository.findById(fromId).orElseThrow();
        repository.findById(toId).orElseThrow();
        from.debit(amount);
        throw new BusinessException("Checked failure after debit (rollbackFor)");
    }

    private void simulateCrash() {
        throw new IllegalStateException("Simulated failure after debit");
    }
}
