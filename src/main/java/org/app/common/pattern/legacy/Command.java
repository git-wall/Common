package org.app.common.pattern.legacy;

public interface Command {
    void execute();
    void undo();
}
