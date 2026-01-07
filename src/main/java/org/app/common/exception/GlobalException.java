package org.app.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.app.common.exception.base.AppException;
import org.app.common.exception.business.NotFoundException;
import org.app.common.res.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    private static final String ERROR_MESSAGE_PATTERN = "Field: %s, Error: %s";

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> exceptionHandler(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> String.format(ERROR_MESSAGE_PATTERN, fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.toList());
        var id = TracingContext.getRequestId();

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity
            .status(status)
            .body(ResponseUtils.Error.build(id, status, status.getReasonPhrase(), errors));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> exceptionHandler(IllegalArgumentException ex) {
        var id = TracingContext.getRequestId();
        String message = String.format("%s - %s", id, ex.getMessage());
        log.error(message, ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
            .status(status)
            .body(ResponseUtils.Error.build(id, status, ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Object> handleTooMany(TooManyRequestsException ex) {
        var id = TracingContext.getRequestId();
        String message = String.format("%s - %s", id, ex.getMessage());
        log.error(message, ex);
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity
            .status(status)
            .body(ResponseUtils.Error.build(id, status, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        var id = TracingContext.getRequestId();
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity
            .status(status)
            .header("X-Error", HttpStatus.NOT_FOUND.getReasonPhrase())
            .body(ResponseUtils.Error.build(id, status, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknown(Exception e) {
        log.error("UNEXPECTED ERROR", e);
        var id = TracingContext.getRequestId();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
            .status(status)
            .body(ResponseUtils.Error.build(id, status, status.getReasonPhrase()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException e) {
        log.error("Id: {}, Context: {}, Type: [{}], Time: {}",
            TracingContext.getRequestId(), e.type(), e.context(), LocalDateTime.now(), e);
        switch (e.type()) {
            case VALIDATION:
                return ResponseEntity.badRequest().build();
            case RATE_LIMIT:
                return ResponseEntity.status(429).build();
            case HTTP_5XX:
            case NETWORK:
            case TIMEOUT:
                return ResponseEntity.status(502).build();
            default:
                return ResponseEntity.status(500).build();
        }
    }
}
