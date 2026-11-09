package org.delcom.ifs25041pbo2026springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;

/**
 * Port repository generik. Interface ini hidup di layer domain; implementasinya
 * berada di layer adapter.
 *
 * @param <T>  tipe entity
 * @param <ID> tipe identitas entity
 */
public interface IRepository<T, ID> {

    /** Menyimpan (atau menimpa) entity dan mengembalikannya. */
    T save(T entity);

    Optional<T> findById(ID id);

    /** Mengembalikan <strong>salinan</strong> daftar entity; mengubahnya tidak memengaruhi penyimpanan. */
    List<T> findAll();

    /** @return {@code true} bila ada entity yang terhapus. */
    boolean deleteById(ID id);

    boolean existsById(ID id);

    long count();
}
