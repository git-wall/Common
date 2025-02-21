package org.app.common.pattern.legacy;

public abstract class TemplateMethod {
    public final void process() {
        before();
        present();
        after();
    }

    protected abstract void before();

    protected abstract void present();

    protected abstract void after();
}
