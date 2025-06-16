package org.app.eav.controller;

import org.app.eav.base.BaseController;
import org.app.eav.entity.Value;
import org.app.eav.entity.dto.ValueDTO;
import org.app.eav.mapper.ValueMapper;
import org.app.eav.service.IValueService;
import org.app.eav.service.ValueService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.app.eav.contain.TagUrl.API_v1;

@RestController
@RequestMapping(API_v1 + ValueController.MAIN_MAPPING)
public class ValueController extends BaseController<ValueDTO, Value, Long> {

    private final IValueService attributeValueService;

    private final ValueMapper valueMapper;

    public static final String MAIN_MAPPING = "value";

    public ValueController(ValueService attributeValueService, ValueMapper valueMapper) {
        super(attributeValueService, valueMapper);
        this.attributeValueService = attributeValueService;
        this.valueMapper = valueMapper;
    }
}
