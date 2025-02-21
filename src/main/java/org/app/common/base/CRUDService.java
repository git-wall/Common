package org.app.common.base;

import org.springframework.data.domain.Page;

public interface CRUDService<I, O, ID> {
    void add(I t);
    void update(I t);
    void delete(ID id);
    O findById(ID id);
    Page<O> findAll(int page, int size);
}
