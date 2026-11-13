package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

/** Task A: declarative transactions and rollback. */
class TransferTransactionTest extends AbstractBankTest {

    @Autowired TransferService transferService;

    @Test
    void successfulTransferMovesMoney() {
        transferService.transfer(ALICE, BOB, money("100.00"));

        System.out.printf("[TASK A] after transfer: Alice=%s Bob=%s%n", balanceOf(ALICE), balanceOf(BOB));
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("900.00");
        assertThat(balanceOf(BOB)).isEqualByComparingTo("600.00");
    }

    @Test
    void insufficientFundsChangesNothing() {
        assertThatThrownBy(() -> transferService.transfer(ALICE, BOB, money("5000.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient funds");

        System.out.printf("[TASK A] after failed transfer: Alice=%s Bob=%s%n", balanceOf(ALICE), balanceOf(BOB));
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1000.00");
        assertThat(balanceOf(BOB)).isEqualByComparingTo("500.00");
    }

    @Test
    void uncheckedExceptionAfterDebitRollsBack() {
        assertThatThrownBy(() -> transferService.transferFailingAfterDebit(ALICE, BOB, money("100.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Simulated failure after debit");

        System.out.printf("[TASK A] after crash between debit/credit: Alice=%s Bob=%s%n",
                balanceOf(ALICE), balanceOf(BOB));
        // the debit was flushed to the DB but rolled back together with the transaction
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1000.00");
        assertThat(balanceOf(BOB)).isEqualByComparingTo("500.00");
    }

    @Test
    void checkedExceptionCommitsByDefault() {
        assertThatThrownBy(() -> transferService.transferFailingWithCheckedException(ALICE, BOB, money("100.00")))
                .isInstanceOf(BusinessException.class);

        System.out.printf("[TASK A] checked exception, default rules: Alice=%s Bob=%s%n",
                balanceOf(ALICE), balanceOf(BOB));
        // Default rule: only RuntimeException/Error roll back -> half of the transfer is committed!
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("900.00");
        assertThat(balanceOf(BOB)).isEqualByComparingTo("500.00");
    }

    @Test
    void checkedExceptionWithRollbackForRollsBack() {
        assertThatThrownBy(() ->
                transferService.transferFailingWithCheckedExceptionRollbackFor(ALICE, BOB, money("100.00")))
                .isInstanceOf(BusinessException.class);

        System.out.printf("[TASK A] checked exception, rollbackFor: Alice=%s Bob=%s%n",
                balanceOf(ALICE), balanceOf(BOB));
        assertThat(balanceOf(ALICE)).isEqualByComparingTo("1000.00");
        assertThat(balanceOf(BOB)).isEqualByComparingTo("500.00");
    }
}
