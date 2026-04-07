package org.app.common.res;

import com.google.gson.annotations.Expose;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiResponse2<T, E extends Enum<E>> extends ApiResponse<T, E> {
    @Expose
    private Object messageDetail;
}
