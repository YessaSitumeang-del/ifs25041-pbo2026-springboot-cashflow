package org.delcom.ifs25041pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;

import java.math.BigDecimal;

/**
 * Port repository khusus transaksi. ID dibuat otomatis oleh repository,
 * sehingga penyimpanan dibedakan menjadi pemasukan dan pengeluaran.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /** Membuat dan menyimpan transaksi bertipe pemasukan dengan ID otomatis. */
    Transaction saveIncome(String description, BigDecimal amount) throws ValidationException;

    /** Membuat dan menyimpan transaksi bertipe pengeluaran dengan ID otomatis. */
    Transaction saveExpense(String description, BigDecimal amount) throws ValidationException;
}
