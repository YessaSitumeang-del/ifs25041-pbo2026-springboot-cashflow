package org.delcom.ifs25041pbo2026springbootcashflow;

import org.delcom.ifs25041pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25041pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25041pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Smoke test: konteks Spring berhasil dimuat dan seluruh bean hasil wiring tersedia. */
@SpringBootTest
class AppTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void compositionRootProvidesAllBeans() {
        assertNotNull(context.getBean(ITransactionRepository.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));
    }
}
