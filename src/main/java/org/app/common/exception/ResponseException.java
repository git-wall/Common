package org.app.common.exception;

import org.app.common.constant.JType;
import org.app.common.utils.JacksonUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

public class ResponseException extends RuntimeException {
    private static final long serialVersionUID = -6931364898754208319L;

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResponseException(Throwable cause) {
        super(cause);
    }

    public static ResponseException from(String format, HttpClientErrorException e) {
        String errorMessage;
        if (e.getStatusCode().is4xxClientError()) {
            Map<String, Object> errorRes = JacksonUtils.readValue(e.getResponseBodyAsString(), JType.MAP_STR_OBJ);
            errorMessage = errorRes.get("errorMessage").toString();
        } else {
            errorMessage = e.getMessage();
        }
        String message = String.format(format, errorMessage);
        return new ResponseException(message);
    }
}
