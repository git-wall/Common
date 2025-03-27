package org.app.common.pattern.revisited;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class DataBus<T> {
    private final List<Consumer<T>> subscribers = new CopyOnWriteArrayList<>();
    private final List<T> messageHistory = new ArrayList<>();
    private final int historySize;

    public DataBus(int historySize) {
        this.historySize = historySize;
    }

    public void subscribe(Consumer<T> subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(T data) {
        if (historySize > 0) {
            synchronized (messageHistory) {
                messageHistory.add(data);
                if (messageHistory.size() > historySize) {
                    messageHistory.remove(0);
                }
            }
        }
        subscribers.forEach(subscriber -> subscriber.accept(data));
    }

    public List<T> getHistory() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory);
        }
    }
}