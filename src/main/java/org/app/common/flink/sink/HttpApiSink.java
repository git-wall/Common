package org.app.common.flink.sink;

import lombok.RequiredArgsConstructor;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.app.common.notification.NotificationInfo;

@RequiredArgsConstructor
public class HttpApiSink<T> implements Sink<T> {
    private static final long serialVersionUID = 6666460143303659329L;
    private final String url;
    private final String token;
    private final NotificationInfo notificationInfo;

    @Override
    public SinkWriter<T> createWriter(WriterInitContext context) {
        return new HttpApiSinkWriter<>(url, token, notificationInfo);
    }
}
