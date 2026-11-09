package org.delcom.ifs25041pbo2026springbootcashflow.config;

import org.delcom.ifs25041pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25041pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

    @Test
    void wiresRepositoryUseCaseAndPresenterTogether() throws Exception {
        AppConfig config = new AppConfig();

        ITransactionRepository repository = config.transactionRepository();
        FinanceUseCase useCase = config.financeUseCase(repository);
        FinancePresenter presenter = config.financePresenter();

        assertNotNull(presenter);
        useCase.addIncome("Gaji", "1000");

        // Use case yang dirakit memakai repository yang sama dengan bean port.
        assertEquals(1L, repository.count());
        assertEquals("Saldo: Rp 1,000.00", presenter.formatBalance(useCase.getBalance()));
        assertEquals(new BigDecimal("1000.00"), useCase.getBalance());
    }
}
