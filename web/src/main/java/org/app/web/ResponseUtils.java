package org.app.web;

import org.slf4j.MDC;

public class ResponseUtils {

    public static class Success {
        public static <T> ApiResponse<T> build(Object id, String code) {
            return  ApiResponse.<T>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .build();
        }

        public static <T> ApiResponse<T> build(Object id, String code, String message) {
            return  ApiResponse.<T>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .message(message)
                    .build();
        }

        public static <T> ApiResponse<T> build(Object id, String code, String message, T data) {
            return  ApiResponse.<T>builder()
                    .id(id)
                    .code(code)
                    .error(false)
                    .message(message)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse2<T> build(Object id, String code, String message, T data, Object messageDetail) {
            return ApiResponse2.<T>builder()
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
        public static <T> ApiResponse<T> build(Object id, String code, String message) {
            return ApiResponse.<T>builder()
                    .id(id)
                    .error(true)
                    .code(code)
                    .message(message)
                    .build();
        }

        public static <T> ApiResponse2<T> build(Object id, String code, String message, Object messageDetailError) {
            return ApiResponse2.<T>builder()
                    .id(id)
                    .error(true)
                    .code(code)
                    .message(message)
                    .messageDetail(messageDetailError)
                    .build();
        }

        public static <T> ApiResponse2<T> build(String code, String message, Object messageDetailError) {
            return ApiResponse2.<T>builder()
                    .id(MDC.get("requestId"))
                    .error(true)
                    .code(code)
                    .message(message)
                    .messageDetail(messageDetailError)
                    .build();
        }

        public static <T> ApiResponse<T> build(String code, String message) {
            return ApiResponse.<T>builder()
                    .id(MDC.get("requestId"))
                    .error(true)
                    .code(code)
                    .message(message)
                    .build();
        }

        public static <T> ApiResponse<T> notfound(String message) {
            return ApiResponse.<T>builder()
                    .id(MDC.get("requestId"))
                    .error(true)
                    .code("404")
                    .message(message)
                    .build();
        }
    }
}
