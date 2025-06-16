package org.app.eav.controller;

import org.app.eav.base.BaseController;
import org.app.eav.entity.Attribute;
import org.app.eav.entity.dto.AttributeDTO;
import org.app.eav.mapper.AttributeMapper;
import org.app.eav.service.IAttributeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attribute")
public class AttributeController extends BaseController<AttributeDTO, Attribute, Integer> {

    private final IAttributeService attributeService;

    private final AttributeMapper attributeMapper;

    protected AttributeController(IAttributeService attributeService, AttributeMapper attributeMapper) {
        super(attributeService, attributeMapper);
        this.attributeService = attributeService;
        this.attributeMapper = attributeMapper;
    }
}
