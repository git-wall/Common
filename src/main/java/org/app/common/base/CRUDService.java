package org.app.common.base;

import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.Optional;

public interface CRUDService<E, ID> {
    E add(E dto);
    
    void addAll(Collection<E> entities, int batchSize);

    E update(E dto);

    void delete(ID id);

    Optional<E> findById(ID id);

    Page<E> findAll(int page, int size);
}
