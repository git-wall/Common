package org.app.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.ThreadContext;
import org.app.common.utils.RequestUtils;
import org.app.common.res.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public abstract class GlobalException {

    private static final String ERROR_MESSAGE_PATTERN = "Field: %s, Error: %s";

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> exceptionHandler(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format(ERROR_MESSAGE_PATTERN, fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.ok(
                ResponseUtils.Error.build(
                        ThreadContext.get(RequestUtils.REQUEST_ID),
                        status,
                        status.getReasonPhrase(),
                        errors)
        );
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> exceptionHandler(IllegalArgumentException ex) {
        var id = ThreadContext.get(RequestUtils.REQUEST_ID);
        String message = String.format("%s - %s", id, ex.getMessage());
        log.error(message, ex);
        return ResponseEntity.ok(
                ResponseUtils.Error.build(
                        id,
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage())
        );
    }
}
