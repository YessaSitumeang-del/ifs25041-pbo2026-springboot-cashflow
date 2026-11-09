package org.delcom.ifs25041pbo2026springbootcashflow.usecase;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Logika aplikasi pencatat keuangan. Kelas ini tidak mengenal UI (tanpa Vaadin
 * dan tanpa cetak ke konsol); kegagalan dilaporkan lewat checked domain exception
 * dan seluruh hasil dikembalikan sebagai entity untuk diformat oleh presenter.
 */
public class FinanceUseCase {

    private static final String ENTITY_NAME = "Transaksi";
    private static final int MAX_AMOUNT_TEXT_LENGTH = 30;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?(\\d+(\\.\\d*)?|\\.\\d+)");

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository wajib diisi");
    }

    // ---------------------------------------------------------------- tambah

    public Transaction addIncome(String description, String amountText) throws ValidationException {
        return addIncome(description, parseAmount(amountText));
    }

    public Transaction addIncome(String description, BigDecimal amount) throws ValidationException {
        return repository.saveIncome(description, amount);
    }

    public Transaction addExpense(String description, String amountText) throws ValidationException {
        return addExpense(description, parseAmount(amountText));
    }

    public Transaction addExpense(String description, BigDecimal amount) throws ValidationException {
        return repository.saveExpense(description, amount);
    }

    // ----------------------------------------------------------------- baca

    /** Seluruh transaksi sesuai urutan input (salinan, aman diubah pemanggil). */
    public List<Transaction> getAll() {
        return repository.findAll();
    }

    /**
     * Mencari berdasarkan keterangan (case-insensitive). Kata kunci kosong
     * mengembalikan semua transaksi. Data tidak diubah.
     */
    public List<Transaction> search(String keyword) {
        List<Transaction> all = repository.findAll();
        if (keyword == null || keyword.isBlank()) {
            return all;
        }
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        return all.stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(needle))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** Seluruh transaksi yang diurutkan memakai comparator milik {@link SortOption}. */
    public List<Transaction> sort(SortOption option) throws ValidationException {
        if (option == null) {
            throw new ValidationException("sort", "Opsi pengurutan tidak valid.");
        }
        List<Transaction> all = repository.findAll();
        all.sort(option.getComparator());
        return all;
    }

    /**
     * Gabungan cari + urut untuk tampilan daftar. {@code option == null} berarti
     * tanpa pengurutan khusus (urutan input).
     */
    public List<Transaction> getTransactions(String keyword, SortOption option) {
        List<Transaction> result = search(keyword);
        if (option != null) {
            result.sort(option.getComparator());
        }
        return result;
    }

    // ---------------------------------------------------------------- hapus

    /** Menghapus berdasarkan ID teks mentah (mis. input pengguna). */
    public Transaction delete(String idText) throws ValidationException, EntityNotFoundException {
        if (idText == null || idText.isBlank()) {
            throw new ValidationException("id", "ID tidak boleh kosong.");
        }
        int id;
        try {
            id = Integer.parseInt(idText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("id", "ID harus berupa bilangan bulat positif.");
        }
        return delete(id);
    }

    /** @return transaksi yang baru dihapus (untuk pesan sukses). */
    public Transaction delete(int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("id", "ID harus berupa bilangan bulat positif.");
        }
        Transaction target = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NAME, id));
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(ENTITY_NAME, id);
        }
        return target;
    }

    // ---------------------------------------------------------------- saldo

    public int getTransactionCount() {
        return (int) repository.count();
    }

    public BigDecimal getTotalIncome() {
        return repository.findAll().stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(zero(), BigDecimal::add);
    }

    public BigDecimal getTotalExpense() {
        return repository.findAll().stream()
                .filter(Transaction::isExpense)
                .map(Transaction::getAmount)
                .reduce(zero(), BigDecimal::add);
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public BigDecimal getBalance() {
        return repository.findAll().stream()
                .map(Transaction::signedAmount)
                .reduce(zero(), BigDecimal::add);
    }

    // ------------------------------------------------------------- internal

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }

    /** Mengubah teks jumlah menjadi angka; menolak kosong dan non-numerik. */
    private static BigDecimal parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.isBlank()) {
            throw new ValidationException("amount", "Jumlah tidak boleh kosong.");
        }
        String text = amountText.trim();
        if (text.length() > MAX_AMOUNT_TEXT_LENGTH || !NUMBER_PATTERN.matcher(text).matches()) {
            throw new ValidationException("amount",
                    "Jumlah harus berupa angka (contoh: 1000000 atau 1500000.50).");
        }
        return new BigDecimal(text);
    }
}
