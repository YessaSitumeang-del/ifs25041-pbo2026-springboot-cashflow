package org.delcom.ifs25041pbo2026springbootcashflow.adapter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    private static final class Item {
        private final int id;
        private final String name;

        private Item(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private InMemoryRepository<Item, Integer> repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository<>(item -> item.id);
    }

    @Test
    void savesAndFindsById() {
        Item item = new Item(1, "a");
        assertSame(item, repository.save(item));

        assertSame(item, repository.findById(1).orElseThrow());
        assertTrue(repository.existsById(1));
        assertEquals(1L, repository.count());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertFalse(repository.findById(99).isPresent());
        assertFalse(repository.findById(null).isPresent());
        assertFalse(repository.existsById(99));
    }

    @Test
    void saveWithExistingIdReplacesEntity() {
        repository.save(new Item(1, "lama"));
        Item replacement = new Item(1, "baru");
        repository.save(replacement);

        assertEquals(1L, repository.count());
        assertSame(replacement, repository.findById(1).orElseThrow());
    }

    @Test
    void findAllKeepsInsertionOrder() {
        Item third = new Item(3, "c");
        Item first = new Item(1, "a");
        Item second = new Item(2, "b");
        repository.save(third);
        repository.save(first);
        repository.save(second);

        assertEquals(List.of(third, first, second), repository.findAll());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.save(new Item(1, "a"));

        List<Item> first = repository.findAll();
        first.clear();

        assertEquals(1L, repository.count());
        assertEquals(1, repository.findAll().size());
        assertNotSame(first, repository.findAll());
    }

    @Test
    void deleteByIdReportsWhetherSomethingWasRemoved() {
        repository.save(new Item(1, "a"));

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertEquals(0L, repository.count());
        assertFalse(repository.existsById(1));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> new InMemoryRepository<Item, Integer>(null));
        assertThrows(NullPointerException.class, () -> repository.save(null));

        InMemoryRepository<Item, Integer> nullIds = new InMemoryRepository<>(item -> null);
        assertThrows(NullPointerException.class, () -> nullIds.save(new Item(1, "a")));
    }
}
