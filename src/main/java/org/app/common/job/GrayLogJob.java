package org.app.common.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.entities.log.RequestLog;
import org.app.common.utils.TokenUtils;

import java.util.concurrent.BlockingQueue;

@Slf4j
@AllArgsConstructor
public class GrayLogJob implements Job {
    private final BlockingQueue<RequestLog> queue;
    private final String application;

    @Override
    public void init() {

    }

    @Override
    public void execute(JobContext ctx) throws Exception {
        try {
            RequestLog entity = queue.take();
            if (entity == RequestLog.EMPTY) {
                ctx.stop();
                return;
            }

            var token = TokenUtils.generateId("log_user", 16);
            var key = application + ":" + token;
            log.info("Key {} Info {}", key, entity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error sending log", e);
        }
    }

    @Override
    public void shutdown() {

    }
}
