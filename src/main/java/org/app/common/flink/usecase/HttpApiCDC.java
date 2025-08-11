package org.app.common.flink.usecase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.app.common.entities.cdc.v2.MessageCDC;
import org.app.common.flink.Env;
import org.app.common.flink.StreamCommon;
import org.app.common.flink.StreamEnvironment;
import org.app.common.flink.kafka.KafkaHelper;
import org.app.common.flink.sink.HttpApiSink;
import org.app.common.notification.NotificationInfo;
import org.app.common.support.SnapShot;

import java.util.List;

// CDC - Change Data Capture for Flink jobs
public class HttpApiCDC {
    public final String JOB_NAME;
    private final List<String> TABLES;
    private final Env type;
    private FilterFunction<MessageCDC.Payload> filter;
    private MapFunction<MessageCDC.Payload, ?> mapper;
    private String url;
    private String token;
    private NotificationInfo notificationInfo;

    public HttpApiCDC(String jobName, List<String> tables, Env type) {
        JOB_NAME = jobName;
        TABLES = tables;
        this.type = type;
    }

    public static HttpApiCDC of(String name, List<String> tables, Env type) {
        return new HttpApiCDC(String.format("Source %s", name), tables, type);
    }

    public HttpApiCDC filter(FilterFunction<MessageCDC.Payload> filter) {
        this.filter = filter;
        return this;
    }

    public HttpApiCDC mapper(MapFunction<MessageCDC.Payload, ?> mapper) {
        this.mapper = mapper;
        return this;
    }

    public HttpApiCDC url(String url) {
        this.url = url;
        return this;
    }

    public HttpApiCDC token(String token) {
        this.token = token;
        return this;
    }

    public HttpApiCDC notificationInfo(NotificationInfo notificationInfo) {
        this.notificationInfo = notificationInfo;
        return this;
    }

    public void run(String bootstrapServers, String topic, String groupId) throws Exception {
        StreamExecutionEnvironment env = StreamEnvironment.buildEnv(type, JOB_NAME, "1.0.0");

        KafkaSource<ObjectNode> ks = KafkaHelper.buildKafkaSource(
                bootstrapServers,
                topic,
                groupId,
                new JSONKeyValueDeserializationSchema(false)
        );

        try {
            Run(env, ks);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void Run(StreamExecutionEnvironment env, KafkaSource<ObjectNode> ks) {
        try {
            DataStream<ObjectNode> streamKafka = env
                    .fromSource(ks, WatermarkStrategy.noWatermarks(), JOB_NAME)
                    .rebalance();

            StreamCommon.mapCDCAndFilterTable(streamKafka, TABLES)
                    .filter(filter)
                    .map(mapper)
                    .sinkTo(new HttpApiSink<>(url, token, notificationInfo))
                    .name(JOB_NAME)
                    .setParallelism(2);

            env.getExecutionPlan();
            env.execute(JOB_NAME);
        } catch (Exception e) {
            SnapShot.exceptionToLine(e, "", null);
        }
    }
}
