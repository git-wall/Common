package org.app.common.trigger;

import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private final ConsumerProcessor consumerProcessor;

    public ConsumerService(ConsumerProcessor consumerProcessor) {
        this.consumerProcessor = consumerProcessor;
    }

    public void processData(Object data, String group) {
        consumerProcessor.processData(data, group);
    }
}
