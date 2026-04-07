package org.app.common.eav.v3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    private Integer id;
    private String table;
    private String name;
    private String code;
    private DataType dataType;
}
