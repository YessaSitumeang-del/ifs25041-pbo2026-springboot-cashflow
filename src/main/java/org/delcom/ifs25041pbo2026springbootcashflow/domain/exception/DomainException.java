package org.delcom.ifs25041pbo2026springbootcashflow.domain.exception;

/**
 * Akar hierarki exception domain. Bersifat <em>checked</em> sehingga pemanggil
 * (view) dipaksa menangani kegagalan bisnis secara eksplisit.
 */
public class DomainException extends Exception {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
