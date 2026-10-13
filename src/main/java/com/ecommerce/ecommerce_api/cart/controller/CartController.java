package com.ecommerce.ecommerce_api.cart.controller;

import com.ecommerce.ecommerce_api.cart.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_api.cart.dto.CartResponse;
import com.ecommerce.ecommerce_api.cart.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.ecommerce_api.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UUID userId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        CartResponse response = service.addItem(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UUID userId) {
        CartResponse response = service.getCart(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantityCartItem(@AuthenticationPrincipal UUID userId,
                                                               @PathVariable UUID productId,
                                                               @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        CartResponse response = service.updateQuantityCartItem(userId, productId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UUID userId,
                                           @PathVariable UUID productId) {
        service.removeItem(userId, productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}