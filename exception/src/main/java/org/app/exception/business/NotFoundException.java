package org.app.exception.business;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;
import org.app.exception.base.AppException;
import org.app.exception.constant.ErrorType;

@Setter
@Getter
@StandardException
public class NotFoundException extends AppException {
    private static final long serialVersionUID = -51484704516215517L;

    public NotFoundException(String ctx, Throwable e) {
        super(ErrorType.NOT_FOUND, ctx, "not found data", e);
    }

    public NotFoundException(String ctx, String m) {
        super(ErrorType.NOT_FOUND, ctx, m, null);
    }

    public static NotFoundException build(String ctx) {
        var m = String.format("%s not found data", ctx);
        return new NotFoundException(ctx, m);
    }
}
