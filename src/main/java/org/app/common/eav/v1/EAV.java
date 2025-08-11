package org.app.common.eav.v1;

public interface EAV {
    String getName();
    Class<?> getType();
    Object getValue();
}
