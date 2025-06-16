package org.app.eav.entity;

import lombok.Data;

@Data
public class AttributeValue {
    private Attribute attribute;
    private Value value;
}
