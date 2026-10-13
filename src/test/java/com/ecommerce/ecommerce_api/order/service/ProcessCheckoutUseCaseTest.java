package com.ecommerce.ecommerce_api.order.service;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_api.order.dto.OrderResponse;
import com.ecommerce.ecommerce_api.order.entity.Order;
import com.ecommerce.ecommerce_api.order.entity.OrderStatus;
import com.ecommerce.ecommerce_api.order.repository.OrderRepository;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import com.ecommerce.ecommerce_api.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessCheckoutUseCaseTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProcessCheckoutUseCase useCase;

    private User createUser() {
        return new User("Lucas Vieira", "lucas-vieira@teste.com", "senha1234");
    }

    private Product createProduct() {
        return new Product("Notebook", "Notebook DELL i5 13° geração", new BigDecimal("3500.00"), 10, new Category("eletronics"));
    }

    private Cart cartWithItem(User user, Product product) {
        Cart cart = new Cart(user);
        cart.addItem(product, 2);

        return cart;
    }

    @Test
    void shouldProcessCheckoutSuccessfully() {
        UUID userId = UUID.randomUUID();

        Product product = createProduct();

        Cart cart = cartWithItem(createUser(), product);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(any())).thenReturn(Optional.of(product));
        when(productRepository.decreaseStock(any(), anyInt())).thenReturn(1);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = useCase.execute(userId);

        assertNotNull(response);
        assertEquals(OrderStatus.PAID, response.status());
        assertEquals(new BigDecimal("7000.00"), response.totalAmount());
        assertTrue(cart.isEmpty());

        verify(cartRepository).save(cart);
        verify(orderRepository).saveAndFlush(any(Order.class));
        verify(productRepository).decreaseStock(any(), eq(2));
    }

    @Test
    void whenCartNotFound_shouldThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId));

        assertEquals("Cart not found.", exception.getMessage());

        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void whenCartIsEmpty_shouldThrowBusinessRuleException() {
        UUID userId = UUID.randomUUID();

        Cart emptyCart = new Cart(createUser());

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(emptyCart));

        var exception = assertThrows(BusinessRuleException.class, () -> useCase.execute(userId));

        assertEquals("Can't checkout with cart is empty.", exception.getMessage());

        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void whenProductNotFound_shouldThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();

        Cart cart = cartWithItem(createUser(), createProduct());

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(any())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId));

        assertEquals("Product not found.", exception.getMessage());

        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void whenInsufficientStock_shouldThrowBusinessRuleException() {
        UUID userId = UUID.randomUUID();

        Product product = createProduct();

        Cart cart = cartWithItem(createUser(), product);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(any())).thenReturn(Optional.of(product));
        when(productRepository.decreaseStock(any(), anyInt())).thenReturn(0);

        var exception = assertThrows(BusinessRuleException.class, () -> useCase.execute(userId));

        assertEquals("Insufficient stock for product Notebook.",  exception.getMessage());

        verify(orderRepository, never()).saveAndFlush(any());
    }
}