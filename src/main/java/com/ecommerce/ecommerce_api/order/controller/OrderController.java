package com.ecommerce.ecommerce_api.order.controller;

import com.ecommerce.ecommerce_api.order.dto.OrdersResponse;
import com.ecommerce.ecommerce_api.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<OrdersResponse> list(Principal principal) {
        OrdersResponse response = service.listByUser(UUID.fromString(principal.getName()));

        return ResponseEntity.ok(response);
    }
}