package org.app.common.base;

import org.springframework.data.domain.Page;

public interface CRUDService<I, O> {
    void add(I t);
    void update(I t);
    void delete(Number id);
    O findById(Number id);
    Page<O> findAll(int page, int size);
}
