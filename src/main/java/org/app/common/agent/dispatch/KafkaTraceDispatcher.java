package org.app.common.agent.dispatch;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.utils.JacksonUtils;

import java.util.Map;
import java.util.Properties;

public class KafkaTraceDispatcher implements TraceDispatcher {
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaTraceDispatcher(String bootstrapServers, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    @Override
    public void dispatch(String traceId, Map<String, Object> context) {
        try {
            producer.send(new ProducerRecord<>(topic, traceId, JacksonUtils.toJson(context)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
