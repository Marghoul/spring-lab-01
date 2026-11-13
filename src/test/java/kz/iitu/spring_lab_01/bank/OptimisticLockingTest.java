package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/** Task D: optimistic locking with @Version. */
class OptimisticLockingTest extends AbstractBankTest {

    @Autowired PlatformTransactionManager txManager;
    @Autowired AccountService accountService;

    @Test
    void staleCopyCannotOverwriteCommittedUpdate() {
        TransactionTemplate tx = new TransactionTemplate(txManager);

        // two independent persistence contexts load the same row (version 0)
        Account copy1 = tx.execute(s -> accounts.findById(ALICE).orElseThrow());
        Account copy2 = tx.execute(s -> accounts.findById(ALICE).orElseThrow());
        System.out.printf("[TASK D] loaded copy1=%s copy2=%s%n", copy1, copy2);

        copy1.credit(money("100.00"));
        Account saved = tx.execute(s -> accounts.save(copy1));
        System.out.printf("[TASK D] first update committed: %s%n", saved);

        copy2.debit(money("300.00"));
        Throwable error = catchThrowable(() -> tx.execute(s -> accounts.save(copy2)));
        System.out.printf("[TASK D] second (stale) update -> %s%n", error.getClass().getSimpleName());

        assertThat(error).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1100.00"); // first update is preserved
        assertThat(jdbc.queryForObject("SELECT version FROM accounts WHERE id = 1", Long.class)).isEqualTo(1L);
    }

    @Test
    void concurrentDepositsWithRetryLoseNoUpdate() throws Exception {
        int threads = 4;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return accountService.depositWithRetry(BOB, money("10.00"));
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get(20, TimeUnit.SECONDS);
        pool.shutdown();

        System.out.printf("[TASK D] Bob after %d concurrent deposits of 10 with retry: %s%n", threads, balanceOf(BOB));
        assertThat(balanceOf(BOB)).isEqualByComparingTo("540.00");
    }
}
