package org.app.common.res;

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

        public static <T, E extends Enum<E>> ApiResponse2<T, E> build(Object id, E code, String message, T data, Object messageDetail) {
            return ApiResponse2.<T, E>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .message(message)
                    .data(data)
                    .messageDetail(messageDetail)
                    .build();
        }
    }

    public static class Error {
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
    }
}
