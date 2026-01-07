package org.app.common.exception.base;

import org.app.common.design.revisited.retry.Retryable;
import org.app.common.entities.ApiResponse;
import org.app.common.exception.business.BusinessException;
import org.app.common.exception.business.NotFoundException;
import org.app.common.exception.constant.ErrorType;
import org.app.common.exception.infra.*;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.util.function.Supplier;

public class HttpSupport {

    public static <T, R extends ApiResponse<T>> T run(Supplier<R> call, String context) {
        try {
            R res = call.get();

            validate(res, context);

            if (!res.isSuccess()) {
                throw mapBusinessError(res, context);
            }

            return res.data();
        } catch (HttpClientErrorException e) {
            throw map4xx(e, context);
        } catch (HttpServerErrorException e) {
            throw new Http5xxException(context, e);
        } catch (ResourceAccessException e) {
            throw mapNetwork(e, context);
        }
    }

    @Retryable(value = {Http5xxException.class, HttpTimeoutException.class, HttpNetworkException.class})
    public static <T, R extends ApiResponse<T>> T runWithRetry(Supplier<R> call, String context) {
        return run(call, context);
    }

    private static void validate(ApiResponse<?> res, String ctx) {
        String template = "%s: %s";
        Assert.notNull(res, String.format(template, ctx, "ApiResponse must not be null"));
    }

    private static AppException mapBusinessError(ApiResponse<?> res, String ctx) {
        var code = ErrorType.fromString(res.errorCode());
        switch (code) {
            case NOT_FOUND:
                return NotFoundException.build(ctx);
            case RATE_LIMIT:
                return new HttpRateLimitException(ctx, null);
            case INVALID_REQUEST:
                return Http4xxException.buildInvalidRequest(ctx);
            default:
                return BusinessException.build(ctx, res.errorMessage());
        }
    }

    private static AppException map4xx(HttpClientErrorException e, String ctx) {
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            return new HttpRateLimitException(ctx, e);
        }

        return new Http4xxException(ctx, e);
    }

    private static AppException mapNetwork(ResourceAccessException e, String ctx) {
        Throwable root = e.getCause();

        if (root instanceof SocketTimeoutException) {
            return new HttpTimeoutException(ctx, e);
        }

        return new HttpNetworkException(ctx, e);
    }
}
