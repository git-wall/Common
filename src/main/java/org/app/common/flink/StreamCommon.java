package org.app.common.flink;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.app.common.entities.cdc.v2.MessageCDC;
import org.app.common.utils.JacksonUtils;

import java.util.List;
import java.util.Optional;

public class StreamCommon {

    /**
     * Maps a DataStream of ObjectNode to MessageCDC.Payload and filters based on the specified table names.
     *
     * @param streamKafka The input DataStream containing ObjectNode messages from Kafka.
     * @param listTable   A list of table names to filter the payloads by.
     * @return A SingleOutputStreamOperator containing filtered MessageCDC.Payload objects.
     */
    public static SingleOutputStreamOperator<MessageCDC.Payload> mapCDCAndFilterTable(
            DataStream<ObjectNode> streamKafka,
            List<String> listTable) {

        return streamKafka
                .map(o -> {
                    MessageCDC message = JacksonUtils.readValue(o.toString(), MessageCDC.class);
                    return message.getValue().getPayload();
                })
                .filter(payload -> {
                    if (payload == null) {
                        return false;
                    }

                    if (payload.getSource() == null) {
                        return false;
                    }

                    return Optional.ofNullable(payload.getSource().getTable())
                            .filter(listTable::contains)
                            .isPresent();
                });
    }
}
