package org.app.common.base.cdc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Getter
@lombok.Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseCDCMessage<T> {
    private T after;

    private T before;

    private Operation op;
}
