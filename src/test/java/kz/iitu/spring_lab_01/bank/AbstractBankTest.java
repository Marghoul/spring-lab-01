package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

/** Resets the seed data (Alice=1000, Bob=500) before every test. */
@SpringBootTest
abstract class AbstractBankTest {

    protected static final long ALICE = 1L;
    protected static final long BOB = 2L;

    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected AccountRepository accounts;

    @BeforeEach
    void resetData() {
        jdbc.update("DELETE FROM audit_log");
        jdbc.update("UPDATE accounts SET balance = 1000.00, version = 0 WHERE id = 1");
        jdbc.update("UPDATE accounts SET balance = 500.00, version = 0 WHERE id = 2");
    }

    protected BigDecimal balanceOf(long id) {
        return jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, id);
    }

    protected static BigDecimal money(String v) {
        return new BigDecimal(v);
    }
}
