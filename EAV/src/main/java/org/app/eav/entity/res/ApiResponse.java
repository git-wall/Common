package org.app.eav.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiResponse<T, E extends Enum<E>> {
    private Object id;
    private E code;
    private boolean error;
    private String message;
    private T data;
}
