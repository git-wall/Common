package org.app.common.pattern.revisited;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.SpringContext;
import org.app.common.thread.AutoRun;
import org.app.common.thread.RunnableProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.function.Consumer;

/**
 * <img src="https://java-design-patterns.com/assets/img/poison-pill-sequence-diagram.1fd1a9ed.png" alt="PoisonPill">
 * <br>
 * <img src="https://java-design-patterns.com/assets/img/producer-consumer-sequence-diagram.70492f95.png" alt"Producer-Consumer ></>
 * Mix PoisonPill And Producer-Consumer
 */
@AutoRun(detail = "This class PoisonPill is auto-registered and stop if have any data in queue like POISON")
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class PoisonPill<T> extends RunnableProvider {
    private String name;
    private Queue<T> queue;
    private Consumer<T> consumer;
    public T POISON; // special object

    public static <T> PoisonPill<T> beanPrototype() {
        return SpringContext.getContext().getBean(PoisonPill.class);
    }

    public void setting(String name, T poison, Queue<T> queue, Consumer<T> consumer) {
        this.name = name.toUpperCase();
        this.queue = queue;
        this.consumer = consumer;
        this.POISON = poison;
    }

    public void offer(T item) {
        queue.offer(item);
    }

    @Override
    protected void before() {
        log.info("{} ready to run", name);
    }

    @Override
    protected void now() {
        T item = queue.poll();
        if (item == null) return;
        if (item.equals(POISON)) {
            log.info("{} received poison pill, exiting", name);
            hook.shutdown();
            return;
        }
        consumer.accept(item);
    }

    @Override
    protected void after() {
        log.info("{} close", name);
    }
}