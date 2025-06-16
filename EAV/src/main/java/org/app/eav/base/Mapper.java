package org.app.eav.base;

public interface Mapper<D, E> {
    E toEntity(D dto);

    D toDto(E entity);
}
