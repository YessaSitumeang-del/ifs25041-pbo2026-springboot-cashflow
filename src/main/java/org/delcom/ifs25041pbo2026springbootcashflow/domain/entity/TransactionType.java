package org.delcom.ifs25041pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi. Label tampilan sengaja tidak ada di sini
 * (tanggung jawab presenter); enum hanya membawa aturan domain berupa tanda saldo.
 */
public enum TransactionType {

    /** Pemasukan: menambah saldo. */
    INCOME(1),

    /** Pengeluaran: mengurangi saldo. */
    EXPENSE(-1);

    private final int balanceSign;

    TransactionType(int balanceSign) {
        this.balanceSign = balanceSign;
    }

    /** +1 untuk pemasukan, -1 untuk pengeluaran. */
    public int getBalanceSign() {
        return balanceSign;
    }
}
