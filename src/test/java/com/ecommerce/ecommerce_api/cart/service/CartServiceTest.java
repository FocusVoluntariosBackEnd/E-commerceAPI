package com.ecommerce.ecommerce_api.cart.service;

import com.ecommerce.ecommerce_api.cart.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_api.cart.dto.CartResponse;
import com.ecommerce.ecommerce_api.cart.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import com.ecommerce.ecommerce_api.user.entity.User;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService service;

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

    private void givenCartAndProduct() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.of(mouse));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void addItem_shouldAddNewItemAndCalculateTotal() {
        givenCartAndProduct();

        CartResponse response = service.addItem(userId, new AddCartItemRequest(mouse.getId(), 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(new BigDecimal("50.00"), response.items().get(0).unitPrice());
        assertEquals(new BigDecimal("100.00"), response.items().get(0).subtotal());
        assertEquals(new BigDecimal("100.00"), response.total());
        verify(cartRepository).save(cart);
    }

    @Test
    void addItem_whenProductAlreadyInCart_shouldSumQuantity() {
        givenCartAndProduct();
        service.addItem(userId, new AddCartItemRequest(mouse.getId(), 2));

        CartResponse response = service.addItem(userId, new AddCartItemRequest(mouse.getId(), 3));

        assertEquals(1, response.items().size());
        assertEquals(5, response.items().get(0).quantity());
        assertEquals(new BigDecimal("250.00"), response.total());
    }

    @Test
    void addItem_shouldCreateCartWhenUserHasNone() {
        User user = mock(User.class);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.of(mouse));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.addItem(userId, new AddCartItemRequest(mouse.getId(), 1));

        assertEquals(1, response.items().size());
        verify(cartRepository, times(2)).save(any(Cart.class)); // criação + item
    }

    @Test
    void addItem_withInsufficientStock_shouldThrowAndNotSave() {
        Product scarce = newProduct("Teclado", "100.00", 2);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(scarce.getId())).thenReturn(Optional.of(scarce));

        var ex = assertThrows(BusinessRuleException.class,
                () -> service.addItem(userId, new AddCartItemRequest(scarce.getId(), 3)));

        assertEquals("Insufficient stock to add to cart.", ex.getMessage());
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItem_whenSumExceedsStock_shouldThrowAndKeepPreviousQuantity() {
        givenCartAndProduct();
        service.addItem(userId, new AddCartItemRequest(mouse.getId(), 8));

        assertThrows(BusinessRuleException.class,
                () -> service.addItem(userId, new AddCartItemRequest(mouse.getId(), 3))); // 8 + 3 > 10

        assertEquals(8, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_withUnknownOrInactiveProduct_shouldThrowNotFound() {
        UUID productId = UUID.randomUUID();
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> service.addItem(userId, new AddCartItemRequest(productId, 1)));

        assertEquals("Product not found.", ex.getMessage());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateQuantity_shouldChangeQuantityAndTotal() {
        givenCartAndProduct();
        service.addItem(userId, new AddCartItemRequest(mouse.getId(), 2));

        CartResponse response = service.updateQuantityCartItem(userId, mouse.getId(),
                new UpdateCartItemQuantityRequest(4));

        assertEquals(4, response.items().get(0).quantity());
        assertEquals(new BigDecimal("200.00"), response.total());
    }

    @Test
    void updateQuantity_aboveStock_shouldThrowAndKeepQuantity() {
        givenCartAndProduct();
        service.addItem(userId, new AddCartItemRequest(mouse.getId(), 2));

        assertThrows(BusinessRuleException.class, () -> service.updateQuantityCartItem(
                userId, mouse.getId(), new UpdateCartItemQuantityRequest(11)));

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void updateQuantity_whenItemNotInCart_shouldThrowNotFound() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(mouse.getId())).thenReturn(Optional.of(mouse));

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.updateQuantityCartItem(
                userId, mouse.getId(), new UpdateCartItemQuantityRequest(1)));

        assertEquals("Cart item not found.", ex.getMessage());
    }

    @Test
    void updateQuantity_whenUserHasNoCart_shouldThrowNotFound() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.updateQuantityCartItem(
                userId, mouse.getId(), new UpdateCartItemQuantityRequest(1)));

        assertEquals("Cart not found.", ex.getMessage());
        verifyNoInteractions(productRepository);
    }

    @Test
    void removeItem_shouldRemoveOnlyTheGivenProduct() {
        Product keyboard = newProduct("Teclado", "100.00", 5);
        cart.addItem(mouse, 1);
        cart.addItem(keyboard, 1);
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));

        service.removeItem(userId, mouse.getId());

        assertEquals(1, cart.getItems().size());
        assertEquals(keyboard.getId(), cart.getItems().get(0).getProduct().getId());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItem_whenItemNotInCart_shouldThrowNotFound() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> service.removeItem(userId, mouse.getId()));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItem_whenUserHasNoCart_shouldThrowNotFound() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.removeItem(userId, mouse.getId()));
    }

    @Test
    void getCart_shouldReturnEmptyCartWithZeroTotalWhenCreatedNow() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.getCart(userId);

        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.total());
    }
}