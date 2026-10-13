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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    private final UUID userId = UUID.randomUUID();
    private Cart cart;
    private Product mouse;

    @BeforeEach
    void setUp() {
        cart = new Cart(mock(User.class));
        mouse = newProduct("Mouse", "50.00", 10);
    }

    private Product newProduct(String name, String price, int stock) {
        Product product = new Product(name, "desc", new BigDecimal(price), stock, new Category("Eletrônicos"));
        // Product.equals compara por id; sem id todos os produtos seriam "iguais"
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        return product;
    }

    @Test
    void execute_shouldCreatePaidOrderDecreaseStockAndClearCart() {
        cart.addItem(mouse, 2);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.of(mouse));
        when(productRepository.decreaseStock(mouse.getId(), 2)).thenReturn(1);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = useCase.execute(userId);

        assertEquals(OrderStatus.PAID, response.status());
        assertEquals(0, new BigDecimal("100.00").compareTo(response.totalAmount()));
        assertTrue(cart.isEmpty());
        verify(productRepository).decreaseStock(mouse.getId(), 2);
        verify(cartRepository).save(cart);
        verify(orderRepository).saveAndFlush(any(Order.class));
    }

    @Test
    void execute_shouldSumTotalOfAllItems() {
        Product keyboard = newProduct("Teclado", "120.50", 5);
        cart.addItem(mouse, 2);
        cart.addItem(keyboard, 1);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.of(mouse));
        when(productRepository.findByIdAndActiveTrue(keyboard.getId())).thenReturn(Optional.of(keyboard));
        when(productRepository.decreaseStock(mouse.getId(), 2)).thenReturn(1);
        when(productRepository.decreaseStock(keyboard.getId(), 1)).thenReturn(1);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = useCase.execute(userId);

        assertEquals(0, new BigDecimal("220.50").compareTo(response.totalAmount()));
    }

    @Test
    void execute_shouldFailWhenCartDoesNotExist() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId));

        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void execute_shouldFailWhenCartIsEmpty() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));

        assertThrows(BusinessRuleException.class, () -> useCase.execute(userId));

        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void execute_shouldFailWhenProductIsNoLongerActive() {
        cart.addItem(mouse, 2);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId));

        verify(productRepository, never()).decreaseStock(any(UUID.class), anyInt());
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void execute_shouldFailWhenStockIsInsufficientAndNotCreateOrder() {
        cart.addItem(mouse, 2);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.of(mouse));
        when(productRepository.decreaseStock(mouse.getId(), 2)).thenReturn(0);

        var ex = assertThrows(BusinessRuleException.class, () -> useCase.execute(userId));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }
}
