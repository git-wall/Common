package org.app.common.eav;

public interface EAV {
    String getName();
    Class<?> getType();
    Object getValue();
}
