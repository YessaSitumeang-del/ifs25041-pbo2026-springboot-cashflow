package org.delcom.ifs25041pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private FinancePresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new FinancePresenter();
    }

    private static Transaction income() throws ValidationException {
        return new Transaction(1, "Gaji Bulanan", new BigDecimal("1000000"), TransactionType.INCOME);
    }

    private static Transaction expense() throws ValidationException {
        return new Transaction(2, "Makan", new BigDecimal("25000"), TransactionType.EXPENSE);
    }

    @Test
    void formatsMoneyWithThousandSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(new BigDecimal("1000000")));
        assertEquals("Rp 4,975,000.00", presenter.formatMoney(new BigDecimal("4975000.00")));
        assertEquals("Rp 0.50", presenter.formatMoney(new BigDecimal("0.5")));
        assertEquals("Rp 999.00", presenter.formatMoney(new BigDecimal("999")));
        assertEquals("Rp 12,345,678,901.99", presenter.formatMoney(new BigDecimal("12345678901.99")));
        assertEquals("Rp 0.00", presenter.formatMoney(BigDecimal.ZERO));
        assertEquals("Rp 0.00", presenter.formatMoney(null));
        assertEquals("Rp -25,000.00", presenter.formatMoney(new BigDecimal("-25000")));
    }

    @Test
    void formatsTransactionLineLikeTheSpecification() throws Exception {
        assertEquals("[1] Gaji Bulanan | Rp 1,000,000.00 | [Pemasukan]", presenter.formatTransaction(income()));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.formatTransaction(expense()));
    }

    @Test
    void formatsBalanceLikeTheSpecification() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new BigDecimal("4975000")));
        assertEquals("Saldo: Rp 0.00", presenter.formatBalance(BigDecimal.ZERO));
    }

    @Test
    void labelsEveryTypeAndSortOption() {
        assertEquals("Pemasukan", presenter.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.typeLabel(TransactionType.EXPENSE));

        Set<String> labels = new HashSet<>();
        for (SortOption option : SortOption.values()) {
            String label = presenter.sortLabel(option);
            assertTrue(!label.isBlank());
            labels.add(label);
        }
        assertEquals(SortOption.values().length, labels.size());
        assertEquals("Jumlah terkecil", presenter.sortLabel(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.sortLabel(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.sortLabel(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.sortLabel(SortOption.EXPENSE_FIRST));
    }

    @Test
    void formatsDateTime() {
        assertEquals("07 Nov 2026 14:23", presenter.formatDateTime(LocalDateTime.of(2026, 11, 7, 14, 23)));
        assertEquals("-", presenter.formatDateTime(null));
    }

    @Test
    void buildsSuccessMessages() throws Exception {
        assertEquals("Pemasukan berhasil ditambahkan: [1] Gaji Bulanan | Rp 1,000,000.00 | [Pemasukan]",
                presenter.formatAdded(income()));
        assertEquals("Pengeluaran berhasil ditambahkan: [2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.formatAdded(expense()));
        assertEquals("Transaksi berhasil dihapus: [2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.formatDeleted(expense()));
        assertEquals("Yakin ingin menghapus transaksi [2] Makan | Rp 25,000.00 | [Pengeluaran]?",
                presenter.formatDeleteConfirmation(expense()));
    }

    @Test
    void buildsFailureMessagesPerExceptionType() {
        DomainException validation = new ValidationException("Keterangan tidak boleh kosong.");
        DomainException notFound = new EntityNotFoundException("Transaksi", 9);
        DomainException generic = new DomainException("rusak");

        assertEquals("Validasi gagal: Keterangan tidak boleh kosong.", presenter.formatFailure(validation));
        assertEquals("Data tidak ditemukan: Transaksi dengan ID 9 tidak ditemukan.",
                presenter.formatFailure(notFound));
        assertEquals("Terjadi kesalahan: rusak", presenter.formatFailure(generic));
    }
}
