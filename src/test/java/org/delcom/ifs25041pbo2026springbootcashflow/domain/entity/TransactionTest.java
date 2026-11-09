package org.delcom.ifs25041pbo2026springbootcashflow.domain.entity;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private static Transaction create(int id, String description, String amount, TransactionType type)
            throws ValidationException {
        return new Transaction(id, description, new BigDecimal(amount), type);
    }

    @Test
    void createsTransactionWithNormalizedValues() throws Exception {
        LocalDateTime time = LocalDateTime.of(2026, 11, 7, 10, 30);
        Transaction t = new Transaction(1, "  Gaji bulanan  ", new BigDecimal("1000000"),
                TransactionType.INCOME, time);

        assertEquals(1, t.getId());
        assertEquals("Gaji bulanan", t.getDescription());
        assertEquals(new BigDecimal("1000000.00"), t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals(time, t.getCreatedAt());
        assertTrue(t.isIncome());
        assertFalse(t.isExpense());
    }

    @Test
    void usesCurrentTimeWhenNotGiven() throws Exception {
        Transaction t = create(1, "Kopi", "15000", TransactionType.EXPENSE);
        assertNotNull(t.getCreatedAt());
        assertTrue(t.isExpense());
        assertFalse(t.isIncome());
    }

    @Test
    void rejectsBlankOrNullDescription() {
        ValidationException blank = assertThrows(ValidationException.class,
                () -> create(1, "   ", "100", TransactionType.INCOME));
        assertEquals("description", blank.getField());
        assertEquals("Keterangan tidak boleh kosong.", blank.getMessage());

        assertThrows(ValidationException.class,
                () -> new Transaction(1, null, BigDecimal.TEN, TransactionType.INCOME));
    }

    @Test
    void rejectsTooLongDescription() throws Exception {
        String max = "a".repeat(Transaction.MAX_DESCRIPTION_LENGTH);
        assertEquals(max, create(1, max, "100", TransactionType.INCOME).getDescription());

        String tooLong = "a".repeat(Transaction.MAX_DESCRIPTION_LENGTH + 1);
        assertThrows(ValidationException.class, () -> create(1, tooLong, "100", TransactionType.INCOME));
    }

    @Test
    void rejectsZeroNegativeAndNullAmount() {
        assertThrows(ValidationException.class, () -> create(1, "x", "0", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> create(1, "x", "-1", TransactionType.INCOME));
        assertThrows(ValidationException.class,
                () -> new Transaction(1, "x", null, TransactionType.INCOME));
    }

    @Test
    void roundsAmountToTwoDecimalsHalfUp() throws Exception {
        assertEquals(new BigDecimal("10.01"), create(1, "x", "10.005", TransactionType.INCOME).getAmount());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> create(1, "x", "0.004", TransactionType.INCOME));
        assertEquals("amount", ex.getField());
    }

    @Test
    void rejectsAmountThatIsTooLarge() throws Exception {
        assertEquals(new BigDecimal("999999999999.99"),
                create(1, "x", "999999999999.99", TransactionType.INCOME).getAmount());
        assertThrows(ValidationException.class, () -> create(1, "x", "1000000000000", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> create(1, "x", "1E+50", TransactionType.INCOME));
    }

    @Test
    void rejectsInvalidIdTypeAndTime() {
        assertThrows(ValidationException.class, () -> create(0, "x", "1", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> create(-3, "x", "1", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> new Transaction(1, "x", BigDecimal.ONE, null));
        assertThrows(ValidationException.class,
                () -> new Transaction(1, "x", BigDecimal.ONE, TransactionType.INCOME, null));
    }

    @Test
    void changeDescriptionUpdatesAndValidates() throws Exception {
        Transaction t = create(1, "Lama", "100", TransactionType.INCOME);
        t.changeDescription("  Baru ");
        assertEquals("Baru", t.getDescription());

        assertThrows(ValidationException.class, () -> t.changeDescription(" "));
        assertEquals("Baru", t.getDescription());
    }

    @Test
    void changeAmountUpdatesAndValidates() throws Exception {
        Transaction t = create(1, "x", "100", TransactionType.EXPENSE);
        t.changeAmount(new BigDecimal("250.5"));
        assertEquals(new BigDecimal("250.50"), t.getAmount());

        assertThrows(ValidationException.class, () -> t.changeAmount(new BigDecimal("-5")));
        assertEquals(new BigDecimal("250.50"), t.getAmount());
    }

    @Test
    void signedAmountIsPositiveForIncomeAndNegativeForExpense() throws Exception {
        assertEquals(new BigDecimal("100.00"), create(1, "x", "100", TransactionType.INCOME).signedAmount());
        assertEquals(new BigDecimal("-100.00"), create(2, "x", "100", TransactionType.EXPENSE).signedAmount());
    }

    @Test
    void equalityIsBasedOnId() throws Exception {
        Transaction a = create(1, "a", "10", TransactionType.INCOME);
        Transaction sameId = create(1, "b", "99", TransactionType.EXPENSE);
        Transaction other = create(2, "a", "10", TransactionType.INCOME);

        assertEquals(a, sameId);
        assertEquals(a.hashCode(), sameId.hashCode());
        assertFalse(a.equals(other));
        assertFalse(a.equals(null));
        assertFalse(a.equals("bukan transaksi"));
        assertTrue(a.equals(a));
        assertTrue(a.toString().contains("id=1"));
    }

    @Test
    void transactionTypeCarriesBalanceSign() {
        assertEquals(1, TransactionType.INCOME.getBalanceSign());
        assertEquals(-1, TransactionType.EXPENSE.getBalanceSign());
    }
}
