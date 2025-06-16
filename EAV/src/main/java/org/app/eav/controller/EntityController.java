package org.app.eav.controller;

import org.app.eav.base.BaseController;
import org.app.eav.entity.Entity;
import org.app.eav.entity.dto.EntityDTO;
import org.app.eav.mapper.EntityMapper;
import org.app.eav.service.EntityService;
import org.app.eav.service.IEntityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entity")
public class EntityController extends BaseController<EntityDTO, Entity, Integer> {

    private final IEntityService entityService;

    private final EntityMapper entityMapper;

    protected EntityController(EntityService entityService, EntityMapper entityMapper) {
        super(entityService, entityMapper);
        this.entityMapper = entityMapper;
        this.entityService = entityService;
    }
}
