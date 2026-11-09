package org.delcom.ifs25041pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25041pbo2026springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementasi {@link IRepository} generik berbasis memori. Urutan penyimpanan
 * dipertahankan, dan {@link #findAll()} selalu mengembalikan salinan list
 * (salinan defensif). Method disinkronkan karena satu instance dipakai bersama
 * oleh banyak sesi browser.
 *
 * @param <T>  tipe entity
 * @param <ID> tipe identitas entity
 */
public class InMemoryRepository<T, ID> implements IRepository<T, ID> {

    private final Map<ID, T> storage = new LinkedHashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor wajib diisi");
    }

    @Override
    public synchronized T save(T entity) {
        Objects.requireNonNull(entity, "entity wajib diisi");
        ID id = Objects.requireNonNull(idExtractor.apply(entity), "ID entity wajib diisi");
        storage.put(id, entity);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized boolean deleteById(ID id) {
        return storage.remove(id) != null;
    }

    @Override
    public synchronized boolean existsById(ID id) {
        return storage.containsKey(id);
    }

    @Override
    public synchronized long count() {
        return storage.size();
    }
}
