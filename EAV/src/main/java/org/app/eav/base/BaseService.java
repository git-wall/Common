package org.app.eav.base;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public abstract class BaseService<E, ID> implements CRUDService<E, ID> {

    protected final CrudRepository<E, ID> crudRepository;

    protected BaseService(CrudRepository<E, ID> crudRepository) {
        this.crudRepository = crudRepository;
    }

    @Override
    public E add(E e) {
        return crudRepository.save(e);
    }

    @Override
    public E update(E e) {
        return crudRepository.save(e);
    }

    @Override
    public void delete(ID id) {
        crudRepository.deleteById(id);
    }

    @Override
    public Optional<E> findById(ID id) {
        return crudRepository.findById(id);
    }

    @Override
    public Iterable<E> findAll() {
        return crudRepository.findAll();
    }
}
