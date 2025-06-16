package org.app.eav.base;

import org.app.eav.entity.res.ResponseUtils;
import org.app.eav.validation.GroupValidation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class BaseController<DTO, E, ID> {

    protected final CRUDService<E, ID> crudService;

    protected final Mapper<DTO, E> mapper;

    protected BaseController(CRUDService<E, ID> crudService, Mapper<DTO, E> mapper) {
        this.crudService = crudService;
        this.mapper = mapper;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> add(@Validated(GroupValidation.CREATE.class) @RequestBody DTO request) {
        var rs = crudService.add(mapper.toEntity(request));
        var rp = mapper.toDto(rs);

        return ResponseEntity
                .ok()
                .body(ResponseUtils.Success.ok(rp));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@Validated(GroupValidation.UPDATE.class) @RequestBody DTO request) {
        var rs = crudService.update(mapper.toEntity(request));
        var rp = mapper.toDto(rs);

        return ResponseEntity
                .ok()
                .body(ResponseUtils.Success.ok(rp));
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable("id") ID id) {
        Assert.notNull(id, "id must not be null");
        crudService.delete(id);

        return ResponseEntity.ok().body(ResponseUtils.Success.ok("Entity deleted successfully"));
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<?> find(@PathVariable ID id) {
        Assert.notNull(id, "id must not be null");

        return crudService.findById(id)
                .map(mapper::toDto)
                .map(ResponseUtils.Success::ok)
                .map(ResponseEntity.ok()::body)
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseUtils.Error.notfound("Entity not found with id: " + id))
                );
    }

    @GetMapping("/findAll")
    public ResponseEntity<?> findAll() {
        var rp = StreamSupport.stream(crudService.findAll().spliterator(), false)
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(ResponseUtils.Success.ok(rp));
    }
}
