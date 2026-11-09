package org.delcom.ifs25041pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveIncomeAssignsIncomeTypeAndSequentialIds() throws Exception {
        Transaction first = repository.saveIncome("Gaji", new BigDecimal("1000"));
        Transaction second = repository.saveIncome("Bonus", new BigDecimal("500"));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(TransactionType.INCOME, first.getType());
        assertEquals(2L, repository.count());
    }

    @Test
    void saveExpenseAssignsExpenseType() throws Exception {
        Transaction expense = repository.saveExpense("Makan", new BigDecimal("25000"));

        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(expense, repository.findById(1).orElseThrow());
    }

    @Test
    void idsAreNotReusedAfterDelete() throws Exception {
        repository.saveIncome("a", BigDecimal.TEN);
        repository.saveIncome("b", BigDecimal.TEN);
        repository.deleteById(2);

        assertEquals(3, repository.saveExpense("c", BigDecimal.ONE).getId());
        assertEquals(List.of(1, 3), repository.findAll().stream().map(Transaction::getId).toList());
    }

    @Test
    void failedValidationDoesNotConsumeIdOrStoreAnything() throws Exception {
        assertThrows(ValidationException.class, () -> repository.saveIncome(" ", BigDecimal.TEN));
        assertThrows(ValidationException.class, () -> repository.saveExpense("x", new BigDecimal("-1")));
        assertEquals(0L, repository.count());

        assertEquals(1, repository.saveIncome("ok", BigDecimal.TEN).getId());
    }
}
