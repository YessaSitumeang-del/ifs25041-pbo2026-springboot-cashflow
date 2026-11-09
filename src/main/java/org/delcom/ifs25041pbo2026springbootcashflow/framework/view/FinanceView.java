package org.delcom.ifs25041pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25041pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25041pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Halaman utama pencatat keuangan. View hanya merangkai komponen Vaadin:
 * seluruh aturan bisnis ada di {@link FinanceUseCase}, seluruh format teks ada di
 * {@link FinancePresenter}, dan kegagalan domain ditangkap lewat try-catch
 * lalu ditampilkan sebagai {@link Notification}.
 *
 * Tampilan (warna, gradien, kartu, dekorasi) diatur lewat kelas CSS "cf-*"
 * yang disuntikkan satu kali saat halaman dibuka (lihat {@link #CSS}).
 */
@Route("")
@PageTitle("Cash Flow | ifs25041")
public class FinanceView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final int NOTIFICATION_DURATION_MS = 4000;

    /** Menyuntikkan stylesheet halaman satu kali saja. */
    private static final String LOAD_STYLE_JS =
            "if (!document.getElementById('cf-style')) {"
            + "  const s = document.createElement('style');"
            + "  s.id = 'cf-style';"
            + "  s.textContent = $0;"
            + "  document.head.appendChild(s);"
            + "}";

    private static final String CSS = """
            :root {
                --lumo-font-family: 'Times New Roman', Times, serif;
                --lumo-primary-color: #2563eb;
                --lumo-primary-text-color: #2563eb;
                /* ukuran dasar: 16px -> 20px, seluruh teks (rem) ikut membesar */
                font-size: 20px;
            }

            /* ---------- latar belakang halaman ---------- */
            html, body {
                min-height: 100%;
                background:
                    radial-gradient(circle at 6% 18%, rgba(147, 197, 253, 0.55) 0, transparent 22%),
                    radial-gradient(circle at 94% 28%, rgba(165, 180, 252, 0.45) 0, transparent 20%),
                    radial-gradient(circle at 12% 88%, rgba(191, 219, 254, 0.80) 0, transparent 26%),
                    radial-gradient(circle at 90% 92%, rgba(186, 230, 253, 0.70) 0, transparent 24%),
                    linear-gradient(180deg, #dbeafe 0%, #eff6ff 45%, #dbeafe 100%);
                background-attachment: fixed;
            }

            .cf-view {
                position: relative;
                z-index: 1;
            }

            /* ---------- dekorasi sudut ---------- */
            .cf-decor-layer {
                position: fixed;
                inset: 0;
                pointer-events: none;
                z-index: -1;
                overflow: hidden;
            }
            .cf-decor {
                position: absolute;
                line-height: 1;
                filter: drop-shadow(0 6px 8px rgba(30, 64, 175, 0.20));
            }
            @media (max-width: 1350px) {
                .cf-decor-layer { display: none; }
            }

            /* ---------- notifikasi (tengah layar) ---------- */
            vaadin-notification-card::part(content) {
                font-size: 1.1rem;
                text-align: center;
            }
            vaadin-notification-card[theme~='cf-blue']::part(overlay) {
                background: linear-gradient(135deg, #1d4ed8, #3b82f6);
                color: #ffffff;
                border-radius: 18px;
                box-shadow: 0 14px 34px rgba(30, 64, 175, 0.45);
                min-width: 440px;
                max-width: 92vw;
            }
            vaadin-notification-card[theme~='cf-blue']::part(content) {
                color: #ffffff;
            }
            .cf-notif-title {
                font-size: 1.2rem;
                font-weight: 700;
                margin-bottom: 6px;
            }
            .cf-notif-detail {
                font-size: 1.05rem;
            }
            .cf-notif-balance {
                font-size: 1.1rem;
                font-weight: 700;
                margin-top: 6px;
            }

            /* ---------- header ---------- */
            .cf-header-icon {
                font-size: 3.4rem;
                line-height: 1;
                filter: drop-shadow(0 6px 8px rgba(30, 64, 175, 0.25));
            }
            .cf-title {
                margin: 0;
                font-size: 2.1rem;
                font-weight: 800;
                color: #1e3a8a;
            }
            .cf-sparkle {
                font-size: 1.6rem;
            }
            .cf-subtitle {
                margin: 0;
                font-size: 0.95rem;
                color: #1e3a8a;
            }

            /* ---------- kartu ringkasan ---------- */
            .cf-card {
                display: flex;
                align-items: center;
                gap: 14px;
                flex: 1 1 210px;
                padding: 14px 18px;
                border-radius: 18px;
                border: 2px solid transparent;
                box-shadow: 0 6px 14px rgba(30, 64, 175, 0.10);
            }
            .cf-card-icon {
                font-size: 2.6rem;
                line-height: 1;
            }
            .cf-card-text {
                display: flex;
                flex-direction: column;
            }
            .cf-card-label {
                font-size: 0.9rem;
                font-weight: 600;
                color: #1e3a8a;
            }
            .cf-card-value {
                font-size: 1.3rem;
                font-weight: 800;
            }
            .cf-card-count   { background: linear-gradient(135deg, #dbeafe, #eff6ff); border-color: #93c5fd; }
            .cf-card-income  { background: linear-gradient(135deg, #bfdbfe, #e0f2fe); border-color: #60a5fa; }
            .cf-card-expense { background: linear-gradient(135deg, #c7d2fe, #e0e7ff); border-color: #818cf8; }
            .cf-card-balance { background: linear-gradient(135deg, #bae6fd, #e0f2fe); border-color: #38bdf8; }
            .cf-card-count .cf-card-value   { color: #1e3a8a; }
            .cf-card-income .cf-card-value  { color: #1d4ed8; }
            .cf-card-expense .cf-card-value { color: #3730a3; }
            .cf-card-balance .cf-card-value { color: #0369a1; }
            .cf-card-balance .cf-card-value.cf-negative { color: #dc2626; }

            /* ---------- toolbar ---------- */
            vaadin-text-field.cf-search::part(input-field),
            vaadin-combo-box.cf-sort::part(input-field) {
                background: #ffffff;
                border-radius: 12px;
                box-shadow: 0 3px 8px rgba(30, 64, 175, 0.12);
            }
            vaadin-button.cf-btn {
                border-radius: 12px;
                font-weight: 600;
                box-shadow: 0 3px 8px rgba(30, 64, 175, 0.18);
            }
            vaadin-button.cf-btn-refresh {
                background: #ffffff;
                color: #2563eb;
            }
            vaadin-button.cf-btn-income {
                background: linear-gradient(180deg, #3b82f6, #1d4ed8);
                color: #ffffff;
            }
            vaadin-button.cf-btn-expense {
                background: linear-gradient(180deg, #1e40af, #1e3a8a);
                color: #ffffff;
            }
            vaadin-button.cf-btn-delete {
                background: linear-gradient(180deg, #ef4444, #dc2626);
                color: #ffffff;
                border-radius: 10px;
                min-width: 36px;
                height: 36px;
                box-shadow: 0 3px 6px rgba(220, 38, 38, 0.35);
            }

            /* ---------- tabel ---------- */
            vaadin-grid.cf-grid {
                background: #ffffff;
                border-radius: 20px;
                border: none;
                overflow: hidden;
                box-shadow: 0 10px 24px rgba(30, 64, 175, 0.15);
                --lumo-contrast-5pct: rgba(59, 130, 246, 0.08);
            }
            vaadin-grid.cf-grid::part(header-cell) {
                background: linear-gradient(180deg, #dbeafe, #eff6ff);
                color: #1e3a8a;
                font-weight: 700;
                font-size: 1rem;
            }
            vaadin-grid.cf-grid::part(body-cell) {
                color: #0f172a;
            }

            .cf-id {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                min-width: 30px;
                height: 30px;
                border-radius: 50%;
                background: #dbeafe;
                color: #1e40af;
                font-weight: 700;
            }
            .cf-desc {
                font-weight: 500;
            }
            .cf-badge {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 4px 12px;
                border-radius: 999px;
                font-size: 0.8rem;
                font-weight: 600;
            }
            .cf-badge-income  { background: #dbeafe; color: #1d4ed8; }
            .cf-badge-expense { background: #e0e7ff; color: #3730a3; }
            """;

    private final transient FinanceUseCase useCase;
    private final transient FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortCombo = new ComboBox<>();

    private final Span totalCountValue = new Span();
    private final Span incomeValue = new Span();
    private final Span expenseValue = new Span();
    private final Span balanceValue = new Span();

    /** Mencegah pembaruan saling-memicu antara combo urut dan urut header kolom. */
    private boolean syncingSort;

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        addClassName("cf-view");
        setWidthFull();
        setMaxWidth("1200px");
        setHeightFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("margin", "0 auto");

        configureGrid();
        add(buildDecor(), buildHeader(), buildSummaryCards(), buildToolbar(), grid);
        expand(grid);

        refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getUI().getPage().executeJs(LOAD_STYLE_JS, CSS);
    }

    // ------------------------------------------------------------ komponen

    /** Ilustrasi sudut (emoji). Ganti dengan gambar sendiri bila ada. */
    private Component buildDecor() {
        Div layer = new Div(
                decor("🪙", "top:6%;left:3%;font-size:54px;transform:rotate(-15deg);"),
                decor("🪙", "top:17%;left:1%;font-size:40px;transform:rotate(10deg);"),
                decor("✨", "top:24%;left:9%;font-size:28px;"),
                decor("💰", "bottom:2%;left:1%;font-size:120px;"),
                decor("🪙", "bottom:1%;left:9%;font-size:56px;"),
                decor("✨", "top:20%;right:3%;font-size:32px;"),
                decor("✨", "top:32%;right:1%;font-size:26px;"),
                decor("📈", "bottom:3%;right:1.5%;font-size:110px;"),
                decor("🌱", "bottom:1%;right:10%;font-size:50px;"));
        layer.addClassName("cf-decor-layer");
        return layer;
    }

    private Span decor(String emoji, String style) {
        Span span = new Span(emoji);
        span.addClassName("cf-decor");
        span.getElement().setAttribute("style", style);
        return span;
    }

    private Component buildHeader() {
        Span walletIcon = new Span("👛");
        walletIcon.addClassName("cf-header-icon");

        H2 title = new H2("Cash Flow Pribadi");
        title.addClassName("cf-title");
        Span sparkle = new Span("✨");
        sparkle.addClassName("cf-sparkle");
        HorizontalLayout titleRow = new HorizontalLayout(title, sparkle);
        titleRow.setPadding(false);
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Paragraph subtitle = new Paragraph(
                "Catat pemasukan dan pengeluaran. Lihat arusnya, dan kontrol keuanganmu "
                        + "dengan lebih mudah.");
        subtitle.addClassName("cf-subtitle");

        Div text = new Div(titleRow, subtitle);

        HorizontalLayout header = new HorizontalLayout(walletIcon, text);
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private Component buildSummaryCards() {
        HorizontalLayout cards = new HorizontalLayout(
                createCard("👛", "Total Transaksi", totalCountValue, "cf-card-count"),
                createCard("📈", "Total Pemasukan", incomeValue, "cf-card-income"),
                createCard("🔻", "Total Pengeluaran", expenseValue, "cf-card-expense"),
                createCard("⚖️", "Saldo", balanceValue, "cf-card-balance"));
        cards.setWidthFull();
        cards.setPadding(false);
        cards.setSpacing(true);
        cards.getStyle().set("flex-wrap", "wrap");
        return cards;
    }

    private Component createCard(String icon, String title, Span value, String variantClass) {
        Span iconSpan = new Span(icon);
        iconSpan.addClassName("cf-card-icon");

        Span label = new Span(title);
        label.addClassName("cf-card-label");
        value.addClassName("cf-card-value");

        Div text = new Div(label, value);
        text.addClassName("cf-card-text");

        Div card = new Div(iconSpan, text);
        card.addClassNames("cf-card", variantClass);
        return card;
    }

    private Component buildToolbar() {
        searchField.addClassName("cf-search");
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(event -> refresh());

        sortCombo.addClassName("cf-sort");
        sortCombo.setPlaceholder("Urutkan...");
        sortCombo.setItems(SortOption.values());
        sortCombo.setItemLabelGenerator(presenter::sortLabel);
        sortCombo.setClearButtonVisible(true);
        sortCombo.setWidth("240px");
        sortCombo.addValueChangeListener(event -> {
            if (syncingSort) {
                return;
            }
            syncingSort = true;
            try {
                grid.sort(Collections.emptyList());
            } finally {
                syncingSort = false;
            }
            refresh();
        });

        Button refreshButton = new Button("Muat Ulang", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClassNames("cf-btn", "cf-btn-refresh");
        refreshButton.addClickListener(event -> refresh());

        Button incomeButton = new Button("Tambah Pemasukan", new Icon(VaadinIcon.PLUS));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        incomeButton.addClassNames("cf-btn", "cf-btn-income");
        incomeButton.addClickListener(event -> openAddDialog(TransactionType.INCOME));

        Button expenseButton = new Button("Tambah Pengeluaran", new Icon(VaadinIcon.MINUS));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        expenseButton.addClassNames("cf-btn", "cf-btn-expense");
        expenseButton.addClickListener(event -> openAddDialog(TransactionType.EXPENSE));

        HorizontalLayout left = new HorizontalLayout(searchField, sortCombo);
        left.setAlignItems(FlexComponent.Alignment.CENTER);
        HorizontalLayout right = new HorizontalLayout(refreshButton, incomeButton, expenseButton);
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout toolbar = new HorizontalLayout(left, right);
        toolbar.setWidthFull();
        toolbar.setPadding(false);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("cf-grid");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setMinHeight("300px");

        grid.addComponentColumn(this::createIdBubble)
                .setHeader("# ID")
                .setKey("id")
                .setComparator(Comparator.comparingInt(Transaction::getId))
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::createDescription)
                .setHeader("📄 Keterangan")
                .setKey("description")
                .setComparator(Comparator.comparing(
                        (Transaction t) -> t.getDescription().toLowerCase(Locale.ROOT)))
                .setFlexGrow(1);

        // Header "Jumlah" dan "Tipe" memakai comparator milik SortOption (domain),
        // sehingga urutan dari header kolom sama dengan urutan dari combo.
        grid.addColumn(t -> presenter.formatMoney(t.getAmount()))
                .setHeader("💰 Jumlah")
                .setKey("amount")
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(SortOption.AMOUNT_ASC.getComparator())
                .setAutoWidth(true);

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("🏷️ Tipe")
                .setKey("type")
                .setComparator(SortOption.INCOME_FIRST.getComparator())
                .setAutoWidth(true);

        grid.addColumn(t -> presenter.formatDateTime(t.getCreatedAt()))
                .setHeader("📅 Waktu")
                .setKey("time")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt))
                .setAutoWidth(true);

        grid.addComponentColumn(this::createDeleteButton)
                .setHeader("⚙️ Aksi")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Mengurutkan lewat header kolom menghapus pilihan di combo urut.
        grid.addSortListener(event -> {
            if (syncingSort || event.getSortOrder().isEmpty()) {
                return;
            }
            syncingSort = true;
            try {
                sortCombo.clear();
            } finally {
                syncingSort = false;
            }
        });
    }

    private Component createIdBubble(Transaction transaction) {
        Span bubble = new Span(String.valueOf(transaction.getId()));
        bubble.addClassName("cf-id");
        return bubble;
    }

    private Component createDescription(Transaction transaction) {
        Span text = new Span(transaction.getDescription());
        text.addClassName("cf-desc");
        return text;
    }

    private Component createTypeBadge(Transaction transaction) {
        boolean income = transaction.isIncome();
        Span arrow = new Span(income ? "⬆" : "⬇");
        Span label = new Span(presenter.typeLabel(transaction.getType()));
        Span badge = new Span(arrow, label);
        badge.addClassNames("cf-badge", income ? "cf-badge-income" : "cf-badge-expense");
        return badge;
    }

    private Component createDeleteButton(Transaction transaction) {
        Button button = new Button(new Icon(VaadinIcon.TRASH));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        button.addClassName("cf-btn-delete");
        button.getElement().setAttribute("aria-label", "Hapus transaksi " + transaction.getId());
        button.addClickListener(event -> confirmDelete(transaction));
        return button;
    }

    // -------------------------------------------------------------- aksi

    private void refresh() {
        List<Transaction> items = useCase.getTransactions(searchField.getValue(), sortCombo.getValue());
        grid.setItems(items);

        totalCountValue.setText(String.valueOf(useCase.getTransactionCount()));
        incomeValue.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseValue.setText(presenter.formatMoney(useCase.getTotalExpense()));

        BigDecimal balance = useCase.getBalance();
        balanceValue.setText(presenter.formatMoney(balance));
        balanceValue.getElement().getClassList().set("cf-negative", balance.signum() < 0);
    }

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setCloseOnOutsideClick(false);

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setPlaceholder(income ? "mis. Gaji bulanan" : "mis. Belanja bulanan");
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah");
        amountField.setPlaceholder("mis. 1000000 atau 1500000.50");
        amountField.setPrefixComponent(new Span("Rp"));
        amountField.setWidthFull();

        FormLayout form = new FormLayout(descriptionField, amountField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidth("360px");
        form.setMaxWidth("100%");
        dialog.add(form);

        Button cancel = new Button("Batal", event -> dialog.close());
        Button save = new Button("Simpan", event ->
                submit(type, descriptionField.getValue(), amountField.getValue(), dialog));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickShortcut(Key.ENTER);
        dialog.getFooter().add(cancel, save);

        dialog.open();
        descriptionField.focus();
    }

    private void submit(TransactionType type, String description, String amountText, Dialog dialog) {
        try {
            Transaction saved = type == TransactionType.INCOME
                    ? useCase.addIncome(description, amountText)
                    : useCase.addExpense(description, amountText);
            dialog.close();
            refresh();
            showSuccess(presenter.typeLabel(saved.getType()) + " berhasil ditambahkan",
                    presenter.formatTransaction(saved));
        } catch (DomainException ex) {
            showError(presenter.formatFailure(ex));
        }
    }

    private void confirmDelete(Transaction transaction) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Hapus transaksi?");
        dialog.setText(presenter.formatDeleteConfirmation(transaction));
        dialog.setCancelable(true);
        dialog.setCancelText("Batal");
        dialog.setConfirmText("Hapus");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> delete(transaction.getId()));
        dialog.open();
    }

    private void delete(int id) {
        try {
            Transaction removed = useCase.delete(id);
            refresh();
            showSuccess("Transaksi berhasil dihapus", presenter.formatTransaction(removed));
        } catch (DomainException ex) {
            // Data mungkin sudah dihapus dari sesi lain, jadi tampilan disegarkan juga.
            refresh();
            showError(presenter.formatFailure(ex));
        }
    }

    // ------------------------------------------------------------ notifikasi

    /**
     * Notifikasi sukses (biru, di tengah layar), tiap bagian di baris sendiri:
     * judul berikon centang, detail transaksi, lalu saldo terbaru.
     */
    private void showSuccess(String title, String detail) {
        Div titleLine = new Div(new Span("✅ " + title));
        titleLine.addClassName("cf-notif-title");
        Div detailLine = new Div(new Span(detail));
        detailLine.addClassName("cf-notif-detail");
        Div balanceLine = new Div(new Span(presenter.formatBalance(useCase.getBalance())));
        balanceLine.addClassName("cf-notif-balance");

        Notification notification = new Notification(new Div(titleLine, detailLine, balanceLine));
        notification.setDuration(NOTIFICATION_DURATION_MS);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.getElement().getThemeList().add("cf-blue");
        notification.open();
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, NOTIFICATION_DURATION_MS,
                Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}