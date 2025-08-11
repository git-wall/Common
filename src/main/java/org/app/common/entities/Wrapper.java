package org.app.common.entities;

import lombok.Data;

@Data
public class Wrapper<T> {
    public T data;
}

