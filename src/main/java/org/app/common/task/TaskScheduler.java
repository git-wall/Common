package org.app.common.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskScheduler {

    @Scheduled(fixedRate = 5000)
    public void fixedRateTask() {
        System.out.println("Run every 5s from start" + LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 5000)
    public void fixedDelayTask() {
        System.out.println("Run every 5s previous ends" + LocalDateTime.now());
    }
}
