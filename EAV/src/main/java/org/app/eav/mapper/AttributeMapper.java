package org.app.eav.mapper;

import org.app.eav.base.Mapper;
import org.app.eav.entity.Attribute;
import org.app.eav.entity.dto.AttributeDTO;
import org.springframework.stereotype.Component;

@Component
public class AttributeMapper implements Mapper<AttributeDTO, Attribute> {
    @Override
    public Attribute toEntity(AttributeDTO dto) {
        return null;
    }

    @Override
    public AttributeDTO toDto(Attribute entity) {
        return null;
    }
}
