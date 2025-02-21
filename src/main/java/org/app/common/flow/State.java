package org.app.common.flow;

public interface State {
    void enter();
    void execute();
    void exit();
}
