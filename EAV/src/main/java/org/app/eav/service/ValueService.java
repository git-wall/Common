package org.app.eav.service;

import org.app.eav.base.BaseService;
import org.app.eav.entity.Value;
import org.app.eav.repo.command.ValueRepo;
import org.springframework.stereotype.Service;

@Service
public class ValueService extends BaseService<Value, Long> implements IValueService {

    private final ValueRepo attributeValueRepo;

    protected ValueService(ValueRepo attributeValueRepo) {
        super(attributeValueRepo);
        this.attributeValueRepo = attributeValueRepo;
    }
}
