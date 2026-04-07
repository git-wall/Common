package org.app.common.support.weblayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
    topics = "test-topic",
    partitions = 1
)
public class Kafka {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TestConsumer consumer;

    @Test
    void shouldConsumeMessage() {
        kafkaTemplate.send("test-topic", "hello kafka");

        await()
            .atMost(5, SECONDS)
            .untilAsserted(() ->
                assertThat(consumer.getMessages())
                    .contains("hello kafka")
            );
    }

    @Component
    public class TestConsumer {

        private final List<String> messages = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = "test-topic", groupId = "test-group")
        public void listen(String message) {
            messages.add(message);
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
