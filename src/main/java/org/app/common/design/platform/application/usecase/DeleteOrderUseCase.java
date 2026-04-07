package org.app.common.design.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import org.app.common.design.platform.repo.OrderRepository;
import org.app.common.exception.business.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;

    @CacheEvict(value = "orders", key = "#id")
    public void execute(String id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException(id);
        }
        orderRepository.deleteById(id);
    }
}
