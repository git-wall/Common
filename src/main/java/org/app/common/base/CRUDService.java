package org.app.common.base;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface CRUDService<DTO, E, ID> {
    E add(DTO dto);

    void addAll(List<E> entities, int batchSize);

    void addBatch_SkipError(List<E> entities, int batchSize);

    void addBatch_RollbackError(List<E> entities, int batchSize);

    E update(DTO dto);

    void delete(ID id);

    Optional<E> findById(ID id);

    Page<E> findAll(int page, int size);
}
