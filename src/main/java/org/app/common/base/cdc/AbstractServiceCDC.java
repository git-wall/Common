package org.app.common.base.cdc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.base.CRUDService;
import org.app.common.base.IDEntity;
import org.app.common.base.mapper.BaseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractServiceCDC
        <Repo extends JpaRepository<E, ID>,
                E extends CDC,
                ID,
                DTO extends IDEntity<ID> & CDC,
                Mapper extends BaseMapper<DTO, E>>
        implements CRUDService<DTO, E, ID> {

    protected final Repo repository;

    protected final Mapper mapper;

    @PersistenceContext
    protected final EntityManager entityManager;

    protected final TransactionTemplate transactionTemplate;

    @Override
    public E add(DTO input) {
        E entity = mapper.dtoToEntity(input);
        return repository.save(entity);
    }

    /**
     * <pre>{@code
     * All-or-Nothing in One Transaction ACID
     *
     * One transaction covers all batches
     * If any error occurs, the entire operation rolls back
     * Connection is opened once and closed at the end
     * Best for data integrity when all records must be saved together
     * }</pre>
     */
    @Override
    @Transactional
    public void addAll(List<E> entities, int batchSize) {
        int size = entities.size();
        for (int i = 0; i < size; i += batchSize) {
            List<E> batch = entities.subList(i, Math.min(i + batchSize, entities.size()));
            repository.saveAll(batch);
            entityManager.flush();
            entityManager.clear();
        }
    }

    /**
     * <pre>{@code
     * Independent Transactions Per Batch
     *
     * Each batch gets its own transaction
     * If one batch fails, only that batch is rolled back
     * Previous successful batches remain committed
     * Each batch opens and closes a connection
     * Good for processing large datasets where some failures are acceptable
     * }</pre>
     */
    @Override
    public void addBatch_SkipError(List<E> entities, int batchSize) {
        int size = entities.size();
        for (int i = 0; i < size; i += batchSize) {
            final int endIndex = Math.min(i + batchSize, entities.size());
            final List<E> batch = entities.subList(i, endIndex);
            try {
                repository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            } catch (Exception e) {
                log.error("Skipp: Error saving batch from index {} to {}: {}", i, endIndex, e.getMessage(), e);
            }
        }
    }

    /**
     * <pre>{@code
     * Single Transaction with Rollback when error
     *
     * One transaction if with each batch error rollback in that transaction else done
     * Use {@link org.springframework.transaction.support.TransactionTemplate} to execute
     * data in transaction with auto rollback if error if you want to see manual rollback look like
     * see the code below
     * }</pre>
     *
     * <pre>{@code
     * // Behind the code : this is manual and the code in func is auto roll back when error
     * transactionTemplate.execute(status -> {
     *         try {
     *             repository.saveAll(batch);
     *             entityManager.flush();
     *             entityManager.clear();
     *             return null;
     *         } catch (Exception e) {
     *             status.setRollbackOnly();
     *         }
     *     });
     * }</pre>
     */
    @Override
    public void addBatch_RollbackError(List<E> entities, int batchSize) {
        int size = entities.size();
        for (int i = 0; i < size; i += batchSize) {
            final int endIndex = Math.min(i + batchSize, size);
            final List<E> batch = entities.subList(i, endIndex);
            try {
                exeBatchWithRollBack(batch);
            } catch (Exception e) {
                log.error("Rollback: Error saving batch from index {} to {}: {}", i, endIndex, e.getMessage(), e);
            }
        }
    }

    private void exeBatchWithRollBack(List<E> batch) {
        transactionTemplate.execute(status -> {
            repository.saveAll(batch);
            entityManager.flush();
            entityManager.clear();
            return null;
        });
    }

    @Override
    public E update(DTO input) {
        Optional<E> e = repository.findById(input.getId());
        Assert.isTrue(e.isPresent(), String.format("Not found object with id: %s", input.getId()));
        Assert.isTrue(
                Objects.equals(e.get().getCdcVersion(), input.getCdcVersion()),
                "Error update, version obj is change. Pls reload and update again"
        );
        E entity = mapper.dtoToEntity(input);
        return repository.save(entity);
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<E> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }
}
