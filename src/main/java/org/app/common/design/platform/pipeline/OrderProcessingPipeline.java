package org.app.common.design.platform.pipeline;

import lombok.RequiredArgsConstructor;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.pipeline.stage.EnrichmentStage;
import org.app.common.design.platform.pipeline.stage.TransformationStage;
import org.app.common.design.platform.pipeline.stage.ValidationStage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProcessingPipeline {

    private final ValidationStage validationStage;
    private final TransformationStage transformationStage;
    private final EnrichmentStage enrichmentStage;

    public Order execute(Order input) {
        return new Pipeline<Order>()
            .addStage(validationStage)
            .addStage(transformationStage)
            .addStage(enrichmentStage)
            .execute(input);
    }
}
