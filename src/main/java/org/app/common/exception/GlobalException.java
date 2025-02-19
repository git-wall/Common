package org.app.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.DecorateContext;
import org.app.common.utils.RequestUtils;
import org.app.common.utils.ResponseUtils;
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

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> exceptionHandler(MethodArgumentNotValidException ex) {
        String format = "Field: %s, Error: %s";
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format(format, fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.ok(
                ResponseUtils.Error.build(
                        DecorateContext.get(RequestUtils.REQUEST_ID),
                        status,
                        status.getReasonPhrase(),
                        errors)
        );
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> exceptionHandler(IllegalArgumentException ex) {
        var id = DecorateContext.get(RequestUtils.REQUEST_ID);
        String message = String.format("Request id: %s, Error: %s", id, ex.getMessage());
        log.error(message, ex);
        return ResponseEntity.ok(
                ResponseUtils.Error.build(
                        id,
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage())
        );
    }
}
