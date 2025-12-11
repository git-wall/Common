package org.app.common.jpa;

public abstract class BaseLockService<T extends BaseLockableEntity, ID> {

    protected final BaseLockRepository<T, ID> repository;

    protected BaseLockService(BaseLockRepository<T, ID> repository) {
        this.repository = repository;
    }

    protected T loadEntity(ID id) {
        return repository.findByIdForOptimistic(id)
            .orElseThrow(() -> new IllegalStateException("Not found"));
    }
}
