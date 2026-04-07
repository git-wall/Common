package org.app.common.design.platform.pipeline.stage;

import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.pipeline.PipelineStage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransformationStage implements PipelineStage<Order> {

    @Override
    public Order process(Order input) {
        log.info("Transforming order data");

        // Normalize data, apply business rules
        input.getItems().forEach(item -> {
            item.setProductName(item.getProductName().trim().toUpperCase());
        });

        return input;
    }
}
