package org.app.common.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractService<E extends IDEntity<ID>, ID> implements CRUDService<E, ID> {

    protected final JpaRepository<E, ID> repository;

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public E add(E e) {
        return repository.save(e);
    }

    @Override
    @Transactional
    public void addAll(Collection<E> entities, int batchSize) {
        List<E> entityList = entities instanceof List ? (List<E>) entities : new java.util.ArrayList<>(entities);
        int size = entityList.size();
        for (int i = 0; i < size; i += batchSize) {
            List<E> batch = entityList.subList(i, Math.min(i + batchSize, size));
            repository.saveAll(batch);
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Override
    @Transactional
    public E update(E e) {
        return findById(e.getId())
                .map(repository::save)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Entity with id %s not found for update", e.getId()))
                );
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<E> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }
}
