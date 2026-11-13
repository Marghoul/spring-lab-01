package kz.iitu.spring_lab_01.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

/**
 * Task D: safe reaction to an optimistic-lock conflict. A deposit computed from a
 * freshly read balance is safe to repeat, so it is retried in a new transaction.
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    static final int MAX_ATTEMPTS = 5;

    private final AccountRepository repository;
    private final TransactionTemplate tx;

    public AccountService(AccountRepository repository, PlatformTransactionManager txManager) {
        this.repository = repository;
        this.tx = new TransactionTemplate(txManager);
    }

    public Account depositWithRetry(Long id, BigDecimal amount) {
        for (int attempt = 1; ; attempt++) {
            try {
                return tx.execute(status -> {
                    Account acc = repository.findById(id).orElseThrow(); // fresh copy
                    acc.credit(amount);
                    return repository.saveAndFlush(acc);
                });
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic lock conflict on account {} (attempt {}/{})", id, attempt, MAX_ATTEMPTS);
                if (attempt >= MAX_ATTEMPTS) throw e;
            }
        }
    }
}
