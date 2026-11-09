package org.delcom.ifs25041pbo2026springbootcashflow.domain.entity;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity transaksi keuangan. {@code id}, {@code type}, dan {@code createdAt}
 * bersifat final; keterangan dan jumlah hanya dapat diubah lewat method bernama
 * jelas ({@link #changeDescription(String)}, {@link #changeAmount(BigDecimal)})
 * yang selalu memvalidasi nilai baru.
 */
public class Transaction {

    public static final int MAX_DESCRIPTION_LENGTH = 100;

    /** Jumlah digit bagian bulat maksimum (sampai 999.999.999.999,99). */
    private static final int MAX_INTEGER_DIGITS = 12;

    private final int id;
    private String description;
    private BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime createdAt;

    public Transaction(int id, String description, BigDecimal amount, TransactionType type)
            throws ValidationException {
        this(id, description, amount, type, LocalDateTime.now());
    }

    public Transaction(int id, String description, BigDecimal amount, TransactionType type,
                       LocalDateTime createdAt) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("id", "ID transaksi harus berupa bilangan bulat positif.");
        }
        if (type == null) {
            throw new ValidationException("type", "Tipe transaksi wajib diisi.");
        }
        if (createdAt == null) {
            throw new ValidationException("createdAt", "Waktu transaksi wajib diisi.");
        }
        this.id = id;
        this.description = normalizeDescription(description);
        this.amount = normalizeAmount(amount);
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    /** Jumlah bertanda: positif untuk pemasukan, negatif untuk pengeluaran. */
    public BigDecimal signedAmount() {
        return amount.multiply(BigDecimal.valueOf(type.getBalanceSign()));
    }

    public void changeDescription(String newDescription) throws ValidationException {
        this.description = normalizeDescription(newDescription);
    }

    public void changeAmount(BigDecimal newAmount) throws ValidationException {
        this.amount = normalizeAmount(newAmount);
    }

    private static String normalizeDescription(String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException("description", "Keterangan tidak boleh kosong.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("description",
                    "Keterangan maksimal " + MAX_DESCRIPTION_LENGTH + " karakter.");
        }
        return trimmed;
    }

    private static BigDecimal normalizeAmount(BigDecimal value) throws ValidationException {
        if (value == null) {
            throw new ValidationException("amount", "Jumlah wajib diisi.");
        }
        // Dicek sebelum setScale agar nilai berpangkat sangat besar tidak membebani memori.
        if (value.precision() - value.scale() > MAX_INTEGER_DIGITS) {
            throw new ValidationException("amount", "Jumlah terlalu besar.");
        }
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        if (scaled.signum() <= 0) {
            throw new ValidationException("amount", "Jumlah harus lebih dari 0.");
        }
        return scaled;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Transaction)) {
            return false;
        }
        return id == ((Transaction) other).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", type=" + type + ", amount=" + amount + "}";
    }
}
