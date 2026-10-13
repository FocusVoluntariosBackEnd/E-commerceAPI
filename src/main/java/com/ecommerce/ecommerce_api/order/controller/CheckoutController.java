package com.ecommerce.ecommerce_api.order.controller;

import com.ecommerce.ecommerce_api.order.dto.OrderResponse;
import com.ecommerce.ecommerce_api.order.service.ProcessCheckoutUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    private final ProcessCheckoutUseCase useCase;

    public CheckoutController(ProcessCheckoutUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UUID userId) {
        OrderResponse response = useCase.execute(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
