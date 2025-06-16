package org.app.eav.service;

import org.app.eav.base.BaseService;
import org.app.eav.entity.Entity;
import org.app.eav.repo.command.EntityRepo;
import org.springframework.stereotype.Service;

@Service
public class EntityService extends BaseService<Entity, Integer> implements IEntityService {

    private final EntityRepo entityRepo;

    protected EntityService(EntityRepo entityRepo) {
        super(entityRepo);
        this.entityRepo = entityRepo;
    }
}
