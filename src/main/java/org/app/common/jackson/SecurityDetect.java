package org.app.common.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/// Hidden all fields and methods for security reasons <br/>
/// Only show field define with {@link com.fasterxml.jackson.annotation.JsonProperty}
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class SecurityDetect {
}
