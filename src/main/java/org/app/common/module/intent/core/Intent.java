package org.app.common.module.intent.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Intent<T> {
    private String intentId;
    private String type;         // PAYMENT / REFUND / ...
    private IntentStatus status;
    private T payload;
    private long createdAt;
}
