package org.app.common.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

// Base class for entities that require optimistic locking and hot row tracking
@MappedSuperclass
@Data
public class BaseLockableEntity {
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "is_hot_row")
    private boolean hotRow;
}
