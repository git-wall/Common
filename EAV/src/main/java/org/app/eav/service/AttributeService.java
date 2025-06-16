package org.app.eav.service;

import org.app.eav.base.BaseService;
import org.app.eav.entity.Attribute;
import org.app.eav.repo.command.AttributeRepo;
import org.springframework.stereotype.Service;

@Service
public class AttributeService extends BaseService<Attribute, Integer> implements IAttributeService {

    private final AttributeRepo attributeRepo;

    private AttributeService(AttributeRepo attributeRepo) {
        super(attributeRepo);
        this.attributeRepo = attributeRepo;
    }
}
