package com.ecommerce.ecommerce_api.cart.service;

import com.ecommerce.ecommerce_api.cart.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_api.cart.dto.CartResponse;
import com.ecommerce.ecommerce_api.cart.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.entity.CartItem;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_api.product.dto.UpdateProductRequest;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCart(userId));

        Product product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        cart.addItem(product, request.quantity());

        cart = cartRepository.save(cart);

        return CartResponse.toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantityCartItem(UUID userId, UUID productId, UpdateCartItemQuantityRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found."));

        Product product = productRepository.findByIdAndActiveTrue(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        cart.updateQuantityItem(product, request.quantity());

        cartRepository.save(cart);

        return CartResponse.toResponse(cart);
    }

    @Transactional
    public CartResponse getCart(UUID userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCart(userId));

        return CartResponse.toResponse(cart);
    }

    @Transactional
    public void removeItem(UUID userId, UUID productId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found."));

        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        cart.removeItem(product);

        cartRepository.save(cart);
    }

    private Cart createCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return cartRepository.save(new Cart(user));
    }
}