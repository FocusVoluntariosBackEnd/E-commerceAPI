package com.ecommerce.ecommerce_api.order.service;

import com.ecommerce.ecommerce_api.order.dto.OrderResponse;
import com.ecommerce.ecommerce_api.order.dto.OrdersResponse;
import com.ecommerce.ecommerce_api.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrdersResponse listByUser(UUID userId) {
        List<OrderResponse> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::toResponse)
                .toList();

        return new OrdersResponse(orders);
    }
}