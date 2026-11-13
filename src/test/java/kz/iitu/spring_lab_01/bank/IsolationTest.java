package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task C: two independent transactions in two threads.
 * T1 reads Alice's balance, T2 updates and COMMITS it, T1 reads again.
 * Reads are done with plain JDBC so the JPA first-level cache cannot hide the result.
 */
class IsolationTest extends AbstractBankTest {

    @Autowired PlatformTransactionManager txManager;

    record Reads(BigDecimal first, BigDecimal second) { }

    private Reads runScenario(int isolationLevel) throws Exception {
        CountDownLatch firstReadDone = new CountDownLatch(1);
        CountDownLatch writerCommitted = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            TransactionTemplate reader = new TransactionTemplate(txManager);
            reader.setIsolationLevel(isolationLevel);
            TransactionTemplate writer = new TransactionTemplate(txManager);

            Future<Reads> t1 = pool.submit(() -> reader.execute(s -> {
                BigDecimal first = balanceOf(ALICE);
                firstReadDone.countDown();
                await(writerCommitted);
                BigDecimal second = balanceOf(ALICE);
                return new Reads(first, second);
            }));

            Future<?> t2 = pool.submit(() -> {
                await(firstReadDone);
                writer.executeWithoutResult(s ->
                        jdbc.update("UPDATE accounts SET balance = balance + 50 WHERE id = ?", ALICE));
                writerCommitted.countDown(); // countDown after commit
            });

            t2.get(10, TimeUnit.SECONDS);
            return t1.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("timeout");
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void readCommittedSeesCommittedChangeOnSecondRead() throws Exception {
        Reads r = runScenario(TransactionDefinition.ISOLATION_READ_COMMITTED);
        System.out.printf("[TASK C] H2 READ_COMMITTED: first read=%s, second read=%s%n", r.first(), r.second());
        assertThat(r.first()).isEqualByComparingTo("1000.00");
        assertThat(r.second()).isEqualByComparingTo("1050.00"); // non-repeatable read
    }

    @Test
    void repeatableReadKeepsTheSameValue() throws Exception {
        Reads r = runScenario(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        System.out.printf("[TASK C] H2 REPEATABLE_READ: first read=%s, second read=%s, after commit=%s%n",
                r.first(), r.second(), balanceOf(ALICE));
        assertThat(r.first()).isEqualByComparingTo("1000.00");
        assertThat(r.second()).isEqualByComparingTo("1000.00"); // snapshot is stable
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1050.00"); // but the update was committed
    }
}
