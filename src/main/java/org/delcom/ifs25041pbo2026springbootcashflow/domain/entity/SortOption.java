package org.delcom.ifs25041pbo2026springbootcashflow.domain.entity;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Opsi pengurutan transaksi. Setiap opsi membawa {@link Comparator}-nya sendiri
 * sehingga view tidak perlu menulis logika pembanding. Seri dipecah dengan ID
 * agar urutan selalu deterministik.
 */
public enum SortOption {

    /** Jumlah terkecil lebih dulu. */
    AMOUNT_ASC(Comparator.<Transaction, BigDecimal>comparing(Transaction::getAmount)
            .thenComparingInt(Transaction::getId)),

    /** Jumlah terbesar lebih dulu. */
    AMOUNT_DESC(Comparator.<Transaction, BigDecimal>comparing(Transaction::getAmount, Comparator.reverseOrder())
            .thenComparingInt(Transaction::getId)),

    /** Pemasukan ditampilkan sebelum pengeluaran. */
    INCOME_FIRST(Comparator.<Transaction, TransactionType>comparing(Transaction::getType)
            .thenComparingInt(Transaction::getId)),

    /** Pengeluaran ditampilkan sebelum pemasukan. */
    EXPENSE_FIRST(Comparator.<Transaction, TransactionType>comparing(Transaction::getType, Comparator.reverseOrder())
            .thenComparingInt(Transaction::getId));

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> getComparator() {
        return comparator;
    }
}
