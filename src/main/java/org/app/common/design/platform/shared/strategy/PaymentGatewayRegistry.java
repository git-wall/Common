package org.app.common.design.platform.shared.strategy;

import org.app.common.design.platform.infrastructure.adapter.payment.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class PaymentGatewayRegistry {

    private final Map<String, PaymentGateway> gateways;

    // Auto-inject all PaymentGateway beans
    public PaymentGatewayRegistry(Map<String, PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway getGateway(String type) {
        return Optional.ofNullable(gateways.get(type))
            .orElseThrow(() -> new IllegalArgumentException("Unknown payment gateway: " + type));
    }
}
