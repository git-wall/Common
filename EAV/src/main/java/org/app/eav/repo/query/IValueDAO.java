package org.app.eav.repo.query;

import org.app.eav.entity.AttributeValue;

import java.util.List;

public interface IValueDAO {
    List<AttributeValue> getAttributeValues();
}
