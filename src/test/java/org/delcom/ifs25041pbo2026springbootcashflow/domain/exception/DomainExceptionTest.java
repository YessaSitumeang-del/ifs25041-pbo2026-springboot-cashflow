package org.delcom.ifs25041pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainExceptionTest {

    @Test
    void domainExceptionIsCheckedAndKeepsCause() {
        assertFalse(RuntimeException.class.isAssignableFrom(DomainException.class));

        Throwable cause = new IllegalStateException("penyebab");
        DomainException ex = new DomainException("gagal", cause);
        assertEquals("gagal", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals("polos", new DomainException("polos").getMessage());
    }

    @Test
    void validationExceptionCarriesOptionalField() {
        ValidationException withoutField = new ValidationException("pesan");
        assertTrue(withoutField instanceof DomainException);
        assertNull(withoutField.getField());
        assertEquals("pesan", withoutField.getMessage());

        ValidationException withField = new ValidationException("amount", "salah");
        assertEquals("amount", withField.getField());
        assertEquals("salah", withField.getMessage());
    }

    @Test
    void entityNotFoundExceptionBuildsClearMessage() {
        EntityNotFoundException ex = new EntityNotFoundException("Transaksi", 7);
        assertTrue(ex instanceof DomainException);
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", ex.getMessage());
        assertEquals("Transaksi", ex.getEntityName());
        assertEquals(7, ex.getId());
    }
}
