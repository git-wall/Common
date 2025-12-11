package org.app.common.module.call_center.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputField {
    private String name;
    private String type;
    private Boolean required = false;
    private Object defaultValue;
    private Boolean runtimeOnly = false;
}