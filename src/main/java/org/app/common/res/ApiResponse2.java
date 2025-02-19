package org.app.common.res;

import com.google.gson.annotations.Expose;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.app.common.annotation.Description;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiResponse2<T, E extends Enum<E>> extends ApiResponse<T, E> {
    @Expose
    @Description(detail = "long message string or list or any")
    private Object messageDetail;
}
