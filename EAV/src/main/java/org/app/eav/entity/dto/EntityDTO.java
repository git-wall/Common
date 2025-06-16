package org.app.eav.entity.dto;

import lombok.Data;
import org.app.eav.validation.GroupValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class EntityDTO {
    @NotNull(groups = GroupValidation.UPDATE.class)
    private Integer id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}
