package org.app.eav.entity.dto;

import lombok.Data;
import org.app.eav.validation.GroupValidation;

import javax.validation.constraints.NotNull;

@Data
public class ValueDTO {
    @NotNull(groups = GroupValidation.UPDATE.class)
    private Long id;
    @NotNull
    private Integer attributeId;
    @NotNull
    private Object value;
}
