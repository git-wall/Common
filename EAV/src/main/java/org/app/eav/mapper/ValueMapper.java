package org.app.eav.mapper;

import org.app.eav.base.Mapper;
import org.app.eav.entity.Value;
import org.app.eav.entity.dto.ValueDTO;
import org.springframework.stereotype.Component;

@Component
public class ValueMapper implements Mapper<ValueDTO, Value> {
    @Override
    public Value toEntity(ValueDTO dto) {
        return null;
    }

    @Override
    public ValueDTO toDto(Value entity) {
        return null;
    }
}
