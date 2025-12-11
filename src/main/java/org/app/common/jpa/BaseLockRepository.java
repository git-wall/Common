package org.app.common.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

@NoRepositoryBean
public interface BaseLockRepository<T, ID> extends JpaRepository<T, ID> {

    // Optimistic lock with version check
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdForOptimistic(@Param("id") ID id);

    // Pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdForPessimistic(@Param("id") ID id);
}
