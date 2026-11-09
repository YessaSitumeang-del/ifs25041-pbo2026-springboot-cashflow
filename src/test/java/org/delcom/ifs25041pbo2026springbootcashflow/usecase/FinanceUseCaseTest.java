package org.delcom.ifs25041pbo2026springbootcashflow.usecase;

import org.delcom.ifs25041pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    private static List<Integer> ids(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getId).toList();
    }

    private void seed() throws Exception {
        useCase.addIncome("Gaji Bulanan", "5000000");   // id 1
        useCase.addExpense("Makan Siang", "25000");     // id 2
        useCase.addIncome("Bonus proyek", "750000");    // id 3
        useCase.addExpense("Bayar listrik", "300000");  // id 4
    }

    // ---------------------------------------------------------- tambah & saldo

    @Test
    void incomeIncreasesAndExpenseDecreasesBalance() throws Exception {
        assertEquals(new BigDecimal("0.00"), useCase.getBalance());

        Transaction income = useCase.addIncome("Gaji", "5000000");
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(new BigDecimal("5000000.00"), useCase.getBalance());

        Transaction expense = useCase.addExpense("Makan", "25000");
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(new BigDecimal("4975000.00"), useCase.getBalance());
    }

    @Test
    void totalsAndCountAreConsistent() throws Exception {
        seed();

        assertEquals(4, useCase.getTransactionCount());
        assertEquals(new BigDecimal("5750000.00"), useCase.getTotalIncome());
        assertEquals(new BigDecimal("325000.00"), useCase.getTotalExpense());
        assertEquals(useCase.getTotalIncome().subtract(useCase.getTotalExpense()), useCase.getBalance());
    }

    @Test
    void balanceCanBecomeNegative() throws Exception {
        useCase.addIncome("Uang saku", "10000");
        useCase.addExpense("Beli buku", "60000");

        assertEquals(new BigDecimal("-50000.00"), useCase.getBalance());
    }

    @Test
    void acceptsBigDecimalOverloads() throws Exception {
        useCase.addIncome("Gaji", new BigDecimal("100"));
        useCase.addExpense("Jajan", new BigDecimal("40"));

        assertEquals(new BigDecimal("60.00"), useCase.getBalance());
    }

    // ------------------------------------------------------------- validasi

    @Test
    void rejectsBlankDescription() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.addIncome("   ", "1000"));
        assertEquals("description", ex.getField());
        assertThrows(ValidationException.class, () -> useCase.addExpense("", "1000"));
        assertThrows(ValidationException.class, () -> useCase.addExpense(null, "1000"));
    }

    @Test
    void rejectsNonNumericAmount() {
        for (String bad : new String[]{"abc", "12abc", "1e5", "1,000", "Rp 1000", "--5", "1.2.3", "+5", " "}) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> useCase.addIncome("x", bad), "teks: " + bad);
            assertEquals("amount", ex.getField());
        }
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", (String) null));
    }

    @Test
    void rejectsNonPositiveAmount() {
        ValidationException zero = assertThrows(ValidationException.class, () -> useCase.addIncome("x", "0"));
        assertEquals("Jumlah harus lebih dari 0.", zero.getMessage());

        ValidationException negative = assertThrows(ValidationException.class,
                () -> useCase.addExpense("x", "-500"));
        assertEquals("Jumlah harus lebih dari 0.", negative.getMessage());

        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "0.00"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", (BigDecimal) null));
    }

    @Test
    void rejectsAbsurdlyLongOrLargeAmount() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "1".repeat(31)));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "9999999999999"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "1E-1000000"));
    }

    @Test
    void acceptsDecimalAmountForms() throws Exception {
        assertEquals(new BigDecimal("1500000.50"), useCase.addIncome("a", "1500000.50").getAmount());
        assertEquals(new BigDecimal("0.50"), useCase.addIncome("b", ".5").getAmount());
        assertEquals(new BigDecimal("7.00"), useCase.addIncome("c", "7.").getAmount());
        assertEquals(new BigDecimal("42.00"), useCase.addIncome("d", "  42  ").getAmount());
    }

    @Test
    void failedAddLeavesStateUntouched() throws Exception {
        useCase.addIncome("Gaji", "1000");
        assertThrows(ValidationException.class, () -> useCase.addExpense("", "10"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "abc"));

        assertEquals(1, useCase.getTransactionCount());
        assertEquals(new BigDecimal("1000.00"), useCase.getBalance());
        assertEquals(2, useCase.addIncome("lagi", "1").getId());
    }

    // ------------------------------------------------------------------ cari

    @Test
    void searchIsCaseInsensitiveAndOnlyMatchesDescription() throws Exception {
        seed();

        assertEquals(List.of(1), ids(useCase.search("gaji")));
        assertEquals(List.of(1), ids(useCase.search("GAJI")));
        assertEquals(List.of(2), ids(useCase.search("  siang ")));
        assertEquals(List.of(1, 2, 4), ids(useCase.search("a")));
        assertTrue(useCase.search("tidak-ada").isEmpty());
        assertTrue(useCase.search("5000000").isEmpty());
    }

    @Test
    void blankSearchReturnsEverything() throws Exception {
        seed();

        assertEquals(4, useCase.search(null).size());
        assertEquals(4, useCase.search("").size());
        assertEquals(4, useCase.search("   ").size());
    }

    @Test
    void searchDoesNotChangeData() throws Exception {
        seed();
        BigDecimal balanceBefore = useCase.getBalance();

        useCase.search("gaji");
        useCase.search("zzz");

        assertEquals(4, useCase.getTransactionCount());
        assertEquals(balanceBefore, useCase.getBalance());
        assertEquals(List.of(1, 2, 3, 4), ids(useCase.getAll()));
    }

    // ---------------------------------------------------------------- urut

    @Test
    void sortUsesDomainComparators() throws Exception {
        seed();

        assertEquals(List.of(2, 4, 3, 1), ids(useCase.sort(SortOption.AMOUNT_ASC)));
        assertEquals(List.of(1, 3, 4, 2), ids(useCase.sort(SortOption.AMOUNT_DESC)));
        assertEquals(List.of(1, 3, 2, 4), ids(useCase.sort(SortOption.INCOME_FIRST)));
        assertEquals(List.of(2, 4, 1, 3), ids(useCase.sort(SortOption.EXPENSE_FIRST)));
    }

    @Test
    void sortRejectsNullOption() {
        ValidationException ex = assertThrows(ValidationException.class, () -> useCase.sort(null));
        assertEquals("sort", ex.getField());
    }

    @Test
    void getTransactionsCombinesSearchAndSort() throws Exception {
        seed();

        assertEquals(List.of(1, 2, 3, 4), ids(useCase.getTransactions(null, null)));
        assertEquals(List.of(4, 2), ids(useCase.getTransactions("a", SortOption.AMOUNT_DESC)
                .stream().filter(Transaction::isExpense).toList()));
        assertEquals(List.of(3, 1), ids(useCase.getTransactions("n", SortOption.AMOUNT_ASC)
                .stream().filter(Transaction::isIncome).toList()));
        assertEquals(List.of(2), ids(useCase.getTransactions("makan", SortOption.INCOME_FIRST)));
    }

    // --------------------------------------------------------------- hapus

    @Test
    void deleteRemovesTransactionAndKeepsBalanceConsistent() throws Exception {
        seed();

        Transaction removed = useCase.delete(2);
        assertEquals("Makan Siang", removed.getDescription());
        assertEquals(3, useCase.getTransactionCount());
        assertEquals(new BigDecimal("5450000.00"), useCase.getBalance());

        useCase.delete("1");
        assertEquals(new BigDecimal("450000.00"), useCase.getBalance());
        assertEquals(List.of(3, 4), ids(useCase.getAll()));
    }

    @Test
    void deleteUnknownIdThrowsEntityNotFound() throws Exception {
        seed();

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.delete(99));
        assertEquals("Transaksi dengan ID 99 tidak ditemukan.", ex.getMessage());
        assertThrows(EntityNotFoundException.class, () -> useCase.delete("99"));
        assertEquals(4, useCase.getTransactionCount());

        useCase.delete(1);
        assertThrows(EntityNotFoundException.class, () -> useCase.delete(1));
    }

    @Test
    void deleteRejectsInvalidIds() throws Exception {
        seed();

        for (String bad : new String[]{"abc", "", "  ", "1.5", "0", "-1", "99999999999"}) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> useCase.delete(bad), "teks: " + bad);
            assertEquals("id", ex.getField());
        }
        assertThrows(ValidationException.class, () -> useCase.delete((String) null));
        assertThrows(ValidationException.class, () -> useCase.delete(0));
        assertThrows(ValidationException.class, () -> useCase.delete(-7));
        assertEquals(4, useCase.getTransactionCount());
    }

    // ---------------------------------------------------- salinan defensif

    @Test
    void returnedListsAreDefensiveCopies() throws Exception {
        seed();

        useCase.getAll().clear();
        useCase.search("gaji").clear();
        useCase.sort(SortOption.AMOUNT_ASC).clear();
        useCase.getTransactions(null, null).clear();

        assertEquals(4, useCase.getTransactionCount());
        assertEquals(4, useCase.getAll().size());
    }

    @Test
    void constructorRequiresRepository() {
        assertThrows(NullPointerException.class, () -> new FinanceUseCase(null));
    }
}
