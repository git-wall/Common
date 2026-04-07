package org.app.common.design.platform.application.usecase;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.infrastructure.acl.LegacyOrderACL;
import org.app.common.design.platform.infrastructure.event.EventPublisher;
import org.app.common.design.platform.infrastructure.event.OrderCreatedEvent;
import org.app.common.design.platform.repo.OrderRepository;
import org.app.common.design.platform.shared.mapper.OrderMapper;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class ImportLegacyOrderUseCase {

    private final LegacyOrderACL legacyOrderACL;  // ← ACL
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse execute(String legacyOrderId) {

        log.info("Importing legacy order: {}", legacyOrderId);

        // ACL làm tất cả việc translate và protect
        Order order = legacyOrderACL.importFromLegacy(legacyOrderId);

        // Save vào DB
        Order saved = orderRepository.save(order);

        // Publish event
        eventPublisher.publish(OrderCreatedEvent.of(saved));

        return OrderMapper.toResponse(saved);
    }
}
