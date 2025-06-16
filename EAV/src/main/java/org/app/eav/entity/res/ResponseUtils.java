package org.app.eav.entity.res;

import org.app.eav.context.TracingContext;
import org.springframework.http.HttpStatus;

public class ResponseUtils {

    public static class Success {
        public static <T, E extends Enum<E>> ApiResponse<T, E> build(Object id, E code) {
            return ApiResponse.<T, E>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .build();
        }

        public static <T, E extends Enum<E>> ApiResponse<T, E> build(Object id, E code, String message) {
            return ApiResponse.<T, E>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .message(message)
                    .build();
        }

        public static <T, E extends Enum<E>> ApiResponse<T, E> build(Object id, E code, String message, T data) {
            return ApiResponse.<T, E>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .message(message)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T, HttpStatus> ok() {
            return ApiResponse.<T, HttpStatus>builder()
                    .id(TracingContext.getRequestId())
                    .code(HttpStatus.OK)
                    .message(HttpStatus.OK.getReasonPhrase())
                    .error(false)
                    .build();
        }

        public static <T> ApiResponse<T, HttpStatus> ok(String message) {
            return ApiResponse.<T, HttpStatus>builder()
                    .id(TracingContext.getRequestId())
                    .code(HttpStatus.OK)
                    .message(message)
                    .error(false)
                    .build();
        }

        public static <T> ApiResponse<T, HttpStatus> ok(T data) {
            return ApiResponse.<T, HttpStatus>builder()
                    .id(TracingContext.getRequestId())
                    .code(HttpStatus.OK)
                    .message(HttpStatus.OK.getReasonPhrase())
                    .error(false)
                    .data(data)
                    .build();
        }
    }

    public static class Error {
        public static <T, E extends Enum<E>> ApiResponse<T, E> build(E code, String message) {
            return ApiResponse.<T, E>builder()
                    .id(TracingContext.getRequestId())
                    .error(true)
                    .code(code)
                    .message(message)
                    .build();
        }

        public static <T, E extends Enum<E>> ApiResponse<T, E> build(Object id, E code, String message) {
            return ApiResponse.<T, E>builder()
                    .id(id)
                    .error(true)
                    .code(code)
                    .message(message)
                    .build();
        }

        public static <T, E extends Enum<E>> ApiResponse2<T, E> build(Object id, E code, String message, Object messageDetailError) {
            return ApiResponse2.<T, E>builder()
                    .id(id)
                    .error(true)
                    .code(code)
                    .message(message)
                    .messageDetail(messageDetailError)
                    .build();
        }

        public static <T, E extends Enum<E>> ApiResponse2<T, E> build(E code, Object messageDetailError) {
            return ApiResponse2.<T, E>builder()
                    .id(TracingContext.getRequestId())
                    .error(true)
                    .code(code)
                    .message(code.name())
                    .messageDetail(messageDetailError)
                    .build();
        }

        public static <T> ApiResponse<T, HttpStatus> notfound(String message) {
            return ApiResponse.<T, HttpStatus>builder()
                    .id(TracingContext.getRequestId())
                    .error(true)
                    .code(HttpStatus.NOT_FOUND)
                    .message(message)
                    .build();
        }
    }
}
