package org.app.common.design.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.api.dto.OrderSearchCriteria;
import org.app.common.design.platform.repo.OrderRepository;
import org.app.common.design.platform.shared.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchOrdersUseCase {

    private final OrderRepository orderRepository;

    public List<OrderResponse> search(OrderSearchCriteria criteria) {

        if (criteria.getCustomerId() != null && criteria.getStatus() != null) {
            return orderRepository.findByCustomerAndStatusAndDateRange(
                    criteria.getCustomerId(),
                    criteria.getStatus(),
                    criteria.getFromDate(),
                    criteria.getToDate()
                ).stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        }

        if (criteria.getCustomerId() != null) {
            return orderRepository.findByCustomerId(criteria.getCustomerId()).stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        }

        if (criteria.getStatus() != null) {
            return orderRepository.findByStatus(criteria.getStatus()).stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
