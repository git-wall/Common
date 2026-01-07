package org.app.common.design.platform.pipeline.stage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.product.Product;
import org.app.common.design.platform.pipeline.PipelineStage;
import org.app.common.design.platform.repo.ProductRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichmentStage implements PipelineStage<Order> {

    private final ProductRepository productRepository;

    @Override
    public Order process(Order input) {
        log.info("Enriching order data");

        // Enrich with product details
        input.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow();
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
        });

        return input;
    }
}
