package org.app.common.exception.business;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;
import org.app.common.exception.base.AppException;
import org.app.common.exception.constant.ErrorType;

@Setter
@Getter
@StandardException
public class NotFoundException extends AppException {
    private static final long serialVersionUID = -51484704516215517L;

    public NotFoundException(String ctx, Throwable c) {
        super(ErrorType.NOT_FOUND, ctx, "not found data", c);
    }

    public NotFoundException(String ctx, String m) {
        super(ErrorType.NOT_FOUND, ctx, m, null);
    }

    public static NotFoundException build(String ctx) {
        var m = String.format("%s not found data", ctx);
        return new NotFoundException(ctx, m);
    }
}
