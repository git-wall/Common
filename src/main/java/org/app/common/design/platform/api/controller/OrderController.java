package org.app.common.design.platform.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.*;
import org.app.common.design.platform.application.facade.OrderFacade;
import org.app.common.design.platform.application.usecase.CreateOrderUseCase;
import org.app.common.design.platform.application.usecase.GetOrderUseCase;
import org.app.common.design.platform.application.usecase.SearchOrdersUseCase;
import org.app.common.design.platform.application.usecase.UpdateOrderUseCase;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.shared.assembler.OrderAssembler;
import org.app.common.design.platform.shared.presenter.OrderPresenter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final SearchOrdersUseCase searchOrdersUseCase;
    private final OrderFacade orderFacade;
    private final OrderPresenter orderPresenter;
    private final OrderAssembler orderAssembler;

    // === SIMPLE CRUD ===
    // Bạn có thể giữ các endpoint CRUD đơn giản trong controller này.
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        var response = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // === COMPLEX OPERATIONS ===
    // Bạn cần 1 layer Facade để xử lý các quy trình phức tạp liên quan đến nhiều use case và dịch vụ khác nhau.
    @PostMapping("/complete")
    public ResponseEntity<OrderDetailResponse> createCompleteOrder(@RequestBody @Valid OrderRequest request) {
        // Uses Facade for complex orchestration
        var response = orderFacade.createCompleteOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // === GET DATA FROM ANY WHERE TO VIEW ===
    // Bạn cần gom dữ liệu từ nhiều nguồn khác nhau để trả về cho client.
    // Assembler
    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String id) {
        var orderResponse = getOrderUseCase.getById(id);
        // Uses Assembler for complex aggregation
        var detailResponse = orderAssembler.toDetailResponse(orderResponse);
        return ResponseEntity.ok(detailResponse);
    }

    // === PRESENTATION LOGIC FOR UI FORMATTING ===
    // Bạn cần sử dụng Presenter để định dạng lại dữ liệu cho giao diện người dùng.
    // Ví dụ: 100.50 → "$100.50" hoặc "100,50 €" hoặc "Đã xác nhận"
    @GetMapping("/{id}/view")
    public ResponseEntity<OrderViewModel> getOrderView(
        @PathVariable String id,
        @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        var order = getOrderUseCase.getById(id);
        // Uses Presenter for UI formatting
        var viewModel = orderPresenter.present(
            // Convert response back to entity for presenter
            Order.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .build(),
            Locale.forLanguageTag(locale)
        );
        return ResponseEntity.ok(viewModel);
    }

    @PostMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
        @RequestBody OrderSearchCriteria criteria) {

        var orders = searchOrdersUseCase.search(criteria);
        return ResponseEntity.ok(orders);
    }
}
