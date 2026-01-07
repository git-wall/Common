package org.app.common.entities;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@NoArgsConstructor
public class ApiResponse<T> implements ResponseTemplate<T> {
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private T data;

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }

    @Override
    public T data() {
        return data;
    }

    public boolean isError() {
        return !success && StringUtils.hasText(errorMessage);
    }

    public static ApiResponse<?> error(String errorCode, String errorMessage) {
        ApiResponse<?> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public static ApiResponse<?> success(Object data) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}

interface ResponseTemplate<T> {
    boolean isSuccess();

    String errorCode();

    String errorMessage();

    T data();
}
