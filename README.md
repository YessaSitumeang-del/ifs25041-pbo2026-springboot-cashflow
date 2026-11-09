# ifs25041-pbo2026-springboot-cashflow

Aplikasi web pencatat transaksi keuangan pribadi (Spring Boot + Vaadin) dengan
Clean Architecture. Data disimpan di memori dan hilang saat aplikasi di-restart.

- Package: `org.delcom.ifs25041pbo2026springbootcashflow`
- Java 21 (target), Spring Boot 3.5.16, Vaadin 24.8.0, JaCoCo 0.8.14. Pipeline Jenkins membangun dengan JDK 25 (`--release 21`).

## Menjalankan

```powershell
.\mvnw.cmd spring-boot:run
```

Buka `http://localhost:8080` (port diambil dari `APP_PORT` di `.env`, default `8080`).
Menjalankan tes + coverage: `.\mvnw.cmd verify` (laporan: `target/site/jacoco/index.html`).

> Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`) sudah disertakan dan **wajib ikut di-commit**,
> karena `Jenkinsfile` menjalankan `./mvnw -B clean verify -Pproduction`.

Coverage JaCoCo wajib **100%** (INSTRUCTION, BRANCH, LINE, METHOD, CLASS) sesuai gate di Jenkins;
paket `framework/**` (UI Vaadin) dan `App` dikecualikan.

## Struktur layer

```
src/main/java/org/delcom/ifs25041pbo2026springbootcashflow/
├── domain/
│   ├── entity/          Transaction, TransactionType, SortOption
│   ├── exception/       DomainException, EntityNotFoundException, ValidationException
│   └── repository/      IRepository<T, ID>, ITransactionRepository
├── usecase/             FinanceUseCase (tambah, cari, urut, hapus, saldo)
├── adapter/
│   ├── repository/      InMemoryRepository<T, ID>, TransactionRepository
│   └── presenter/       FinancePresenter (format uang, baris, saldo, pesan)
├── framework/view/      FinanceView (Vaadin, @Route(""))
├── config/              AppConfig (composition root, @Bean)
├── DotEnv.java          memuat .env
└── App.java             entry point (memanggil DotEnv.load())
```

Arah dependensi: `domain` <- `usecase` <- `adapter` / `framework`.

## Skenario uji manual

| Aksi | Hasil yang diharapkan |
|---|---|
| Tambah pemasukan `Gaji` / `5000000` | Baris baru bertipe Pemasukan, saldo `Rp 5,000,000.00` |
| Tambah pengeluaran `Makan` / `25000` | Saldo `Rp 4,975,000.00` |
| Keterangan kosong | Notifikasi error "Validasi gagal: Keterangan tidak boleh kosong." |
| Jumlah `-5`, `0`, atau `abc` | Notifikasi error validasi jumlah; data tidak berubah |
| Cari `gaji` / `GAJI` | Grid terfilter (case-insensitive); data dan saldo tidak berubah |
| Urut lewat combo atau header kolom | Memakai comparator `SortOption` |
| Hapus -> konfirmasi | Transaksi hilang, saldo ikut diperbarui |
| Restart aplikasi | Semua data kembali kosong |
