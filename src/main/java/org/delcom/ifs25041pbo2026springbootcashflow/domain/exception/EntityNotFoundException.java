package org.delcom.ifs25041pbo2026springbootcashflow.domain.exception;

/** Dilempar ketika entity dengan ID tertentu tidak ditemukan di repository. */
public class EntityNotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    private final String entityName;
    private final transient Object id;

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan.");
        this.entityName = entityName;
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getId() {
        return id;
    }
}
