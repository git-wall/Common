package org.app.eav.entity.dto;

import lombok.Data;
import org.app.eav.contain.DataSourceType;
import org.app.eav.contain.ScopeType;
import org.app.eav.contain.ValueType;
import org.app.eav.validation.EnumValid;
import org.app.eav.validation.GroupValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AttributeDTO {
    @NotNull(groups = GroupValidation.UPDATE.class, message = "Attribute ID cannot be null")
    private Integer id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @EnumValid(enumClass = ValueType.class)
    private ValueType valueType;
    @EnumValid(enumClass = DataSourceType.class)
    private DataSourceType dataSourceType;
    @EnumValid(enumClass = ScopeType.class)
    private ScopeType scope;
    @NotNull
    private Integer entityId;
}
