package org.app.common.engine.condition;

import lombok.Getter;
import lombok.Setter;
import org.app.common.engine.contain.Operator;
import org.app.common.engine.contain.ValueType;
import org.app.common.utils.ObjUtils;

import java.util.List;

@Setter
@Getter
public class Condition{
    private long id;
    private String name;
    private Operator operator;
    private List<Object> values;
    private ValueType valueType;
}
