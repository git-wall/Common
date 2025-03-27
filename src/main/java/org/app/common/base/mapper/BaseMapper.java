package org.app.common.base.mapper;

public interface BaseMapper<DTO, E> {
    DTO toDTO(E entity);

    E toEntity(DTO dto);
}