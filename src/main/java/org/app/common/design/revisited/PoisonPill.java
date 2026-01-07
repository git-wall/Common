package org.app.common.design.revisited;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.SpringContext;
import org.app.common.thread.AutoRun;
import org.app.common.thread.RunnableProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * <img src="https://java-design-patterns.com/assets/img/poison-pill-sequence-diagram.1fd1a9ed.png" alt="PoisonPill">
 * <br>
 * <img src="https://java-design-patterns.com/assets/img/producer-consumer-sequence-diagram.70492f95.png" alt"Producer-Consumer"></>
 * Mix PoisonPill And Producer-Consumer
 */
@AutoRun(detail = "This class PoisonPill is auto-registered and stop if have any data in queue like POISON")
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class PoisonPill<T> extends RunnableProvider {
    private String name;
    private BlockingQueue<T> queue;
    private Consumer<T> consumer;
    private T poison; // empty object

    @SuppressWarnings({"unchecked"})
    public static <T> PoisonPill<T> beanPrototype() {
        return SpringContext.getContext().getBean(PoisonPill.class);
    }

    public void setting(String name, T poison, Consumer<T> consumer) {
        this.name = name.toUpperCase();
        this.queue = new LinkedBlockingQueue<>();
        this.consumer = consumer;
        this.poison = poison;
    }

    public void offer(T item) {
        if (item == null) {
            log.warn("{}: Cannot offer null item", name);
            return;
        }
        queue.add(item);
    }

    @Override
    protected void before() {
        log.info("{} ready to run", name);
    }

    @Override
    protected void now() {
        try {
            T item = queue.take();
            if (item.equals(poison)) {
                log.info("{} received poison pill, exiting", name);
                hook.shutdown();
                return;
            }

            consumer.accept(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("{} error processing item", name, e);
        }
    }

    @Override
    protected void after() {
        log.info("{} close", name);
    }
}
