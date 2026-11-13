package kz.iitu.spring_lab_01.bank;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Task E: schema versioning with Flyway. */
class FlywayMigrationTest extends AbstractBankTest {

    @Autowired Flyway flyway;

    @Test
    void allMigrationsAreAppliedInOrder() {
        List<Map<String, Object>> history = jdbc.queryForList(
                "SELECT \"version\", \"description\", \"success\" FROM \"flyway_schema_history\" ORDER BY \"installed_rank\"");
        history.forEach(row -> System.out.println("[TASK E] " + row));

        // H2: Flyway also records a version-less "<< Flyway Schema History table created >>" row
        assertThat(history).extracting(r -> r.get("version"))
                .filteredOn(v -> v != null)
                .containsExactly("1", "2", "3", "4");
        assertThat(history).allMatch(r -> Boolean.TRUE.equals(r.get("success")));
        assertThat(jdbc.queryForObject("SELECT status FROM accounts WHERE id = 1", String.class))
                .isEqualTo("ACTIVE");
    }

    @Test
    void secondMigrateRunExecutesNothing() {
        int executed = flyway.migrate().migrationsExecuted;
        System.out.println("[TASK E] migrations executed on second run: " + executed);
        assertThat(executed).isZero();
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void ownerNameIndexFromV4Exists() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE INDEX_NAME = 'IDX_ACCOUNTS_OWNER_NAME'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
