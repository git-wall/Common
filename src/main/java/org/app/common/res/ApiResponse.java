package org.app.common.res;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.app.common.annotation.Description;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiResponse<T, E extends Enum<E>> {
    @Expose
    @Description(detail = "id of request")
    private Object id;
    @Expose
    @Description(detail = "Code of message with type ENUM")
    private E code;
    @Expose
    @Description(detail = "have error or not")
    private boolean error;
    @Expose
    @Description(detail = "short message")
    private String message;
    @Expose
    @Description(detail = "response data")
    private T data;
}
