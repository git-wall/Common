package org.app.message.engine;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.SneakyThrows;
import org.app.message.send.Sender;
import org.app.observation.log.RequestLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DisruptorEngine implements Engine {

    private final RingBuffer<LogEvent> ringBuffer;
    private final Sender sender;

    public DisruptorEngine(Sender sender) {
        this.sender = sender;
        Disruptor<LogEvent> disruptor =
            new Disruptor<>(
                new LogEventFactory(),
                1024 * 64,
                Executors.defaultThreadFactory()
            );

        disruptor.handleEventsWith(new LogEventHandler(sender));

        disruptor.start();

        ringBuffer = disruptor.getRingBuffer();
    }

    @Override
    public void publish(RequestLog log) {
        long seq = ringBuffer.next();

        try {
            LogEvent event = ringBuffer.get(seq);
            event.log = log;
        } finally {
            ringBuffer.publish(seq);
        }
    }

    @Override
    @SneakyThrows
    public void shutdown() {
        sender.close();
    }

    public static class LogEvent {
        RequestLog log;
    }

    public static class LogEventFactory implements EventFactory<LogEvent> {
        @Override
        public LogEvent newInstance() {
            return new LogEvent();
        }
    }

    public static class LogEventHandler implements EventHandler<LogEvent> {

        private final Sender sender;

        private final List<RequestLog> batch = new ArrayList<>(100);

        public LogEventHandler(Sender sender) {
            this.sender = sender;
        }

        @Override
        public void onEvent(LogEvent event, long sequence, boolean endOfBatch) {
            batch.add(event.log);

            if (batch.size() >= 100 || endOfBatch) {
                sender.send(batch);
                batch.clear();
            }
        }
    }
}
