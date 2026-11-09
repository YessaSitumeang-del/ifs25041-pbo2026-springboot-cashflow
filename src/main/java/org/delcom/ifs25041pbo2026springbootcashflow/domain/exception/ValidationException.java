package org.delcom.ifs25041pbo2026springbootcashflow.domain.exception;

/**
 * Dilempar ketika input melanggar aturan domain (keterangan kosong, jumlah
 * non-positif / non-numerik, ID tidak valid, dan sebagainya).
 */
public class ValidationException extends DomainException {

    private static final long serialVersionUID = 1L;

    private final String field;

    public ValidationException(String message) {
        this(null, message);
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    /** Nama field yang gagal divalidasi; {@code null} bila tidak terkait field tertentu. */
    public String getField() {
        return field;
    }
}
