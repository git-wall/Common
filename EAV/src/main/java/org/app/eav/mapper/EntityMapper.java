package org.app.eav.mapper;

import org.app.eav.base.Mapper;
import org.app.eav.entity.Entity;
import org.app.eav.entity.dto.EntityDTO;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper implements Mapper<EntityDTO, Entity> {
    @Override
    public Entity toEntity(EntityDTO dto) {
        return null;
    }

    @Override
    public EntityDTO toDto(Entity entity) {
        return null;
    }
}
