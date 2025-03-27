package org.app.common.base.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class CaptureDataChangeEntity<T extends Serializable> extends AbstractEntity<T> {

    private static final long serialVersionUID = -3821678677306727806L;

    @Version
    protected Integer version;
}
