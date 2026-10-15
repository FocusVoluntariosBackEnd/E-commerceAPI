package com.ecommerce.ecommerce_api.order.service;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.entity.CartItem;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_api.order.dto.OrderResponse;
import com.ecommerce.ecommerce_api.order.entity.Order;
import com.ecommerce.ecommerce_api.order.entity.OrderItem;
import com.ecommerce.ecommerce_api.order.repository.OrderRepository;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessCheckoutUseCase {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse execute(UUID userId) {
        Cart cart = validateCart(userId);

        Order order = new Order(cart.getUser());

        convertItems(cart, order);

        clearAndSaveCart(cart);

        order.calculateTotalAmount();

        orderRepository.saveAndFlush(order);

        return OrderResponse.toResponse(order);
    }

    private Cart validateCart(UUID userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found."));

        if (cart.isEmpty())
            throw new BusinessRuleException("Can't checkout with cart is empty.");

        return cart;
    }

    private void convertItems(Cart cart, Order order) {
        for (CartItem cartItem : cart.getItemsSortedByProductId()) {
            Product product = productRepository.findByIdAndActiveTrue(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

            int updatesProductStock = productRepository.decreaseStock(product.getId(), cartItem.getQuantity());

            if (updatesProductStock == 0)
                throw new BusinessRuleException("Insufficient stock for product " + cartItem.getProduct().getName() + ".");

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());

            order.addItem(orderItem);
        }
    }

    private void clearAndSaveCart(Cart cart) {
        cart.clear();
        cartRepository.save(cart);
    }
}
