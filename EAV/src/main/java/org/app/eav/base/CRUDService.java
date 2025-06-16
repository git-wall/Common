package org.app.eav.base;

import java.util.Optional;

public interface CRUDService<E, ID> {
    E add(E e);

    E update(E e);

    void delete(ID id);

    Optional<E> findById(ID id);

    Iterable<E> findAll();
}
