package org.app.common.module.intent.spi;

import org.app.common.module.intent.core.Intent;

public interface IntentStrategy<T> {

    String type(); // PAYMENT / REFUND

    Class<T> payloadType();

    void executeAsync(Intent<T> intent);
}
