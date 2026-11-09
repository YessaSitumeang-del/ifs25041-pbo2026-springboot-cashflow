package org.delcom.ifs25041pbo2026springbootcashflow.domain.entity;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SortOptionTest {

    private static Transaction tx(int id, String amount, TransactionType type) throws ValidationException {
        return new Transaction(id, "t" + id, new BigDecimal(amount), type);
    }

    private static List<Transaction> sample() throws ValidationException {
        return new ArrayList<>(List.of(
                tx(1, "100", TransactionType.INCOME),
                tx(2, "300", TransactionType.EXPENSE),
                tx(3, "200", TransactionType.INCOME),
                tx(4, "300", TransactionType.INCOME),
                tx(5, "50", TransactionType.EXPENSE)));
    }

    private static List<Integer> sortedIds(SortOption option) throws ValidationException {
        List<Transaction> list = sample();
        list.sort(option.getComparator());
        List<Integer> ids = new ArrayList<>();
        for (Transaction t : list) {
            ids.add(t.getId());
        }
        return ids;
    }

    @Test
    void hasFourOptionsEachWithComparator() {
        assertEquals(4, SortOption.values().length);
        for (SortOption option : SortOption.values()) {
            assertNotNull(option.getComparator());
        }
    }

    @Test
    void amountAscendingPutsSmallestFirstAndBreaksTiesById() throws Exception {
        assertEquals(List.of(5, 1, 3, 2, 4), sortedIds(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingPutsLargestFirstAndBreaksTiesById() throws Exception {
        assertEquals(List.of(2, 4, 3, 1, 5), sortedIds(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstGroupsIncomeBeforeExpense() throws Exception {
        assertEquals(List.of(1, 3, 4, 2, 5), sortedIds(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirstGroupsExpenseBeforeIncome() throws Exception {
        assertEquals(List.of(2, 5, 1, 3, 4), sortedIds(SortOption.EXPENSE_FIRST));
    }
}
