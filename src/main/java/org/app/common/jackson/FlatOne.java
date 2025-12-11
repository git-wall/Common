package org.app.common.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/// be careful when using this class with generic types
/// this will not work with WILL NOT WORK for structured types like {@link java.util.Map} or {@code JsonNode}
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlatOne<T> {
    @JsonUnwrapped
    private T data;
}
