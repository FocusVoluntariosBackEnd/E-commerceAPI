package com.ecommerce.ecommerce_api.order.service;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.order.dto.OrderResponse;
import com.ecommerce.ecommerce_api.order.entity.Order;
import com.ecommerce.ecommerce_api.order.entity.OrderStatus;
import com.ecommerce.ecommerce_api.order.repository.OrderRepository;
import com.ecommerce.ecommerce_api.order.service.ProcessCheckoutUseCase;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import com.ecommerce.ecommerce_api.user.entity.User;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "api.security.jwt.secret=chave_de_teste_com_mais_de_32_caracteres_ok",
        "api.security.jwt.expiration-millis=3600000"
})
class CheckoutIT {

    @Autowired
    private ProcessCheckoutUseCase useCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User user;
    private Category category;
    private Product mouse;
    private Product teclado;

    @BeforeEach
    void setUp() {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        user = userRepository.saveAndFlush(new User("Teste " + tag, "checkout-" + tag + "@test.com", "hash"));
        category = categoryRepository.saveAndFlush(new Category("Cat-" + tag));
        mouse = productRepository.saveAndFlush(
                new Product("Mouse-" + tag, "desc", new BigDecimal("50.00"), 5, category));
        teclado = productRepository.saveAndFlush(
                new Product("Teclado-" + tag, "desc", new BigDecimal("150.00"), 3, category));
    }

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        cartRepository.findByUserIdWithItems(user.getId()).ifPresent(cartRepository::delete);
        productRepository.deleteAllById(List.of(mouse.getId(), teclado.getId()));
        categoryRepository.deleteById(category.getId());
        userRepository.deleteById(user.getId());
    }

    private void givenCartWith(int mouseQuantity, int tecladoQuantity) {
        Cart cart = new Cart(user);
        cart.addItem(mouse, mouseQuantity);
        cart.addItem(teclado, tecladoQuantity);
        cartRepository.saveAndFlush(cart);
    }

    private int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getStock();
    }

    @Test
    void checkout_shouldCommitOrderDecreaseStockAndEmptyCart() {
        givenCartWith(2, 1);

        OrderResponse response = useCase.execute(user.getId());

        assertEquals(OrderStatus.PAID, response.status());
        assertEquals(0, new BigDecimal("250.00").compareTo(response.totalAmount()));
        assertEquals(3, stockOf(mouse));
        assertEquals(2, stockOf(teclado));

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertEquals(1, orders.size());

        Cart cart = cartRepository.findByUserIdWithItems(user.getId()).orElseThrow();
        assertTrue(cart.isEmpty(), "o carrinho deve estar vazio depois do checkout");
    }

    @Test
    void checkout_shouldRollbackEverythingWhenStockRunsOut() {
        givenCartWith(2, 2);

        // outro comprador leva o estoque do teclado depois do item entrar no carrinho
        Product current = productRepository.findById(teclado.getId()).orElseThrow();
        current.updateProduct(current.getName(), current.getDescription(), current.getPrice(),
                1, category, current.getVersion());
        productRepository.saveAndFlush(current);

        assertThrows(BusinessRuleException.class, () -> useCase.execute(user.getId()));

        assertEquals(5, stockOf(mouse), "estoque do mouse não pode ter sido alterado");
        assertEquals(1, stockOf(teclado), "estoque do teclado não pode ter sido alterado");
        assertTrue(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty(),
                "nenhum pedido pode ter sido gravado");

        Cart cart = cartRepository.findByUserIdWithItems(user.getId()).orElseThrow();
        assertEquals(2, cart.getItems().size(), "o carrinho deve continuar intacto");
    }
}