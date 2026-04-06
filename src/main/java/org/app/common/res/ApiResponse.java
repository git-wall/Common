package org.app.common.res;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiResponse<T, E extends Enum<E>> {
    @Expose
    private Object id;
    @Expose
    private E code;
    @Expose
    private boolean error;
    @Expose
    private String message;
    @Expose
    private T data;
}
