package org.app.common.base.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 4537314052420976320L;

    @CreatedDate
    @Column(updatable = false)
    protected LocalDateTime createdAt;
    
    @LastModifiedDate
    protected LocalDateTime updatedAt;
    
    @Column(length = 50)
    protected String createdBy;
    
    @Column(length = 50)
    protected String updatedBy;
}