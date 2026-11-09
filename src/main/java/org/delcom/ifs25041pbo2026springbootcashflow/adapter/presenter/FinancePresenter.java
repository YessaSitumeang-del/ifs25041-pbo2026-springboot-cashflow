package org.delcom.ifs25041pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Presenter: satu-satunya tempat format tampilan (uang, baris transaksi, saldo,
 * label, dan pesan sukses/gagal/validasi). Entity dan use case tidak tahu cara
 * data ditampilkan.
 */
public class FinancePresenter {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"));

    /** Contoh: {@code 1000000} menjadi {@code Rp 1,000,000.00}. */
    public String formatMoney(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return "Rp " + String.format(Locale.US, "%,.2f", value);
    }

    /** Contoh: {@code [1] Gaji | Rp 1,000,000.00 | [Pemasukan]}. */
    public String formatTransaction(Transaction transaction) {
        return String.format(Locale.US, "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                typeLabel(transaction.getType()));
    }

    /** Contoh: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatDateTime(LocalDateTime time) {
        return time == null ? "-" : TIME_FORMAT.format(time);
    }

    public String typeLabel(TransactionType type) {
        return switch (type) {
            case INCOME -> "Pemasukan";
            case EXPENSE -> "Pengeluaran";
        };
    }

    public String sortLabel(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    // --------------------------------------------------------------- pesan

    public String formatAdded(Transaction transaction) {
        String kind = transaction.isIncome() ? "Pemasukan" : "Pengeluaran";
        return kind + " berhasil ditambahkan: " + formatTransaction(transaction);
    }

    public String formatDeleted(Transaction transaction) {
        return "Transaksi berhasil dihapus: " + formatTransaction(transaction);
    }

    public String formatDeleteConfirmation(Transaction transaction) {
        return "Yakin ingin menghapus transaksi " + formatTransaction(transaction) + "?";
    }

    public String formatFailure(DomainException exception) {
        if (exception instanceof ValidationException) {
            return "Validasi gagal: " + exception.getMessage();
        }
        if (exception instanceof EntityNotFoundException) {
            return "Data tidak ditemukan: " + exception.getMessage();
        }
        return "Terjadi kesalahan: " + exception.getMessage();
    }
}
