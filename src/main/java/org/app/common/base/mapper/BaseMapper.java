package org.app.common.base.mapper;

public interface BaseMapper<D ,E> {
    D toDTO(E entity);

    E toEntity(D dto);
}