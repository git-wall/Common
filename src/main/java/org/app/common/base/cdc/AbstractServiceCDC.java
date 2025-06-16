package org.app.common.base.cdc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.base.CRUDService;
import org.app.common.base.IDEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractServiceCDC<E extends IDEntity<ID> & CDC, ID>
        implements CRUDService<E, ID> {

    protected final JpaRepository<E, ID> repository;

    protected final TransactionTemplate transactionTemplate;

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
        }
    }

    @Override
    @Transactional
    public E update(E r) {
        Optional<E> entity = findById(r.getId());
        Assert.isTrue(entity.isPresent(), String.format("Not found object by id: %s", r.getId()));
        return entity
                .filter(e -> e.getCdcVersion().equals(r.getCdcVersion()))
                .map(repository::save)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error update, version is change. Pls reload and update again"
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
