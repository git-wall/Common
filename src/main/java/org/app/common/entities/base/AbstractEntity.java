package org.app.common.entities.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
/**
 * With entities not for tracking
 * */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -3431183429634526645L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected T id;

    @CreationTimestamp
    @Column(name = "created_at")
    protected Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected Instant updatedAt;
}
