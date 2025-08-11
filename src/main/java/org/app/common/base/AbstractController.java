package org.app.common.base;

import lombok.RequiredArgsConstructor;
import org.app.common.base.mapper.BaseMapper;
import org.app.common.validation.GroupValidation;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Optional;

import static org.app.common.constant.TagURL.*;

@RequiredArgsConstructor
public abstract class AbstractController<DTO, E, ID> {

    protected final CRUDService<E, ID> service;

    protected final BaseMapper<DTO, E> mapper;

    @PostMapping(value = CREATE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<E> create(@Validated(GroupValidation.CREATE.class) @RequestBody DTO input) {
        return ResponseEntity.ok(service.add(mapper.toEntity(input)));
    }

    @PutMapping(value = UPDATE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<E> update(@Validated(GroupValidation.UPDATE.class) @RequestBody DTO input) {
        return ResponseEntity.ok(service.update(mapper.toEntity(input)));
    }

    @DeleteMapping(value = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable("id") ID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = FIND_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<E> findById(@NotNull @PathVariable("id") ID id) {
        Optional<E> output = service.findById(id);
        Assert.isTrue(output.isPresent(), String.format("Not found object by id: %s", id));
        return ResponseEntity.ok(output.get());
    }

    @GetMapping(value = FIND_ALL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<E>> findAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.findAll(page, size));
    }
}
