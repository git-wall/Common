package org.app.common.jackson.append;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.app.common.utils.RequestUtils;

@JsonAppend(
    attrs = {
        @JsonAppend.Attr(RequestUtils.REQUEST_ID),
        @JsonAppend.Attr(VirtualProps.TRACE_ID),
        @JsonAppend.Attr(VirtualProps.TIMESTAMP)
    }
)
public abstract class GlobalJsonMixin implements JsonMixin{
    // no fields/methods, just annotations
}
