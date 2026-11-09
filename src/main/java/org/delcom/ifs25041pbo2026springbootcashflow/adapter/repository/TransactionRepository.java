package org.delcom.ifs25041pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;

/**
 * Repository transaksi berbasis memori dengan ID otomatis (1, 2, 3, ...).
 * ID hanya terpakai jika transaksi lolos validasi, dan tidak didaur ulang
 * setelah transaksi dihapus.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    private int nextId = 1;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public synchronized Transaction saveIncome(String description, BigDecimal amount)
            throws ValidationException {
        return create(description, amount, TransactionType.INCOME);
    }

    @Override
    public synchronized Transaction saveExpense(String description, BigDecimal amount)
            throws ValidationException {
        return create(description, amount, TransactionType.EXPENSE);
    }

    private Transaction create(String description, BigDecimal amount, TransactionType type)
            throws ValidationException {
        Transaction transaction = new Transaction(nextId, description, amount, type);
        save(transaction);
        nextId++;
        return transaction;
    }
}
