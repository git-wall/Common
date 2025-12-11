package org.app.common.module.call_center.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputField {
    private String field;
    private String alias;
    private String type;
}