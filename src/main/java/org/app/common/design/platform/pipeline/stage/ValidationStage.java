package org.app.common.design.platform.pipeline.stage;

import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.pipeline.PipelineStage;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;


@Component
@Slf4j
public class ValidationStage implements PipelineStage<Order> {

    @Override
    public Order process(Order input) {
        log.info("Validating order data");

        if (input.getItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }

        if (input.getCustomerId() == null) {
            throw new ValidationException("Customer ID is required");
        }

        return input;
    }
}
