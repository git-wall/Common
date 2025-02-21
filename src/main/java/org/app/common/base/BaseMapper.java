package org.app.common.base;

public interface BaseMapper<E, D> {
    D toDto(E entity);

    E toEntity(D dto);
}
