package org.app.common.base;

import lombok.RequiredArgsConstructor;
import org.app.common.interceptor.log.InterceptorLog;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractController<SERVICE extends CRUDService<DTO, E, ID>, DTO, E, ID> {

    protected final SERVICE service;

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @InterceptorLog(type = InterceptorLog.LogType.LOG)
    public ResponseEntity<E> create(@RequestBody DTO input) {
        return ResponseEntity.ok(service.add(input));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @InterceptorLog(type = InterceptorLog.LogType.LOG)
    public ResponseEntity<E> update(@RequestBody DTO input) {
        return ResponseEntity.ok(service.update(input));
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @InterceptorLog(type = InterceptorLog.LogType.LOG)
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/findById/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @InterceptorLog(type = InterceptorLog.LogType.LOG)
    public ResponseEntity<E> findById(@NotNull @PathVariable ID id) {
        Optional<E> output = service.findById(id);
        Assert.isTrue(output.isPresent(), String.format("Not found object by id: %s", id));
        return ResponseEntity.ok(output.get());
    }

    @GetMapping(value = "/findAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<E>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.findAll(page, size));
    }
}