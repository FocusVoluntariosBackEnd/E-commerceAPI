package com.ecommerce.ecommerce_api.cart.service;

import com.ecommerce.ecommerce_api.cart.dto.CartResponse;
import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.user.entity.User;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getCart(UUID userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCart(userId));

        return CartResponse.toResponse(cart);
    }

    private Cart createCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return cartRepository.saveAndFlush(new Cart(user));
    }
}