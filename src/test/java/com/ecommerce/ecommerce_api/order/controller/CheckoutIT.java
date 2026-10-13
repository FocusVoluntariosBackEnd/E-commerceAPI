package com.ecommerce.ecommerce_api.order.controller;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.order.dto.OrderResponse;
import com.ecommerce.ecommerce_api.order.entity.OrderStatus;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração do checkout contra o PostgreSQL real (mesmo banco do ProductRepositoryIT).
 * Não é @Transactional de propósito: o commit/rollback precisa acontecer de verdade.
 */
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
    private JdbcTemplate jdbc;

    private User user;
    private Category category;
    private Product mouse;
    private Product keyboard;

    @BeforeEach
    void setUp() {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        user = userRepository.saveAndFlush(new User("Teste " + tag, tag + "@example.com", "hash"));
        category = categoryRepository.saveAndFlush(new Category("Cat-" + tag));
        mouse = productRepository.saveAndFlush(new Product("Mouse-" + tag, "desc", new BigDecimal("50.00"), 10, category));
        keyboard = productRepository.saveAndFlush(new Product("Teclado-" + tag, "desc", new BigDecimal("120.50"), 10, category));
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)", user.getId());
        jdbc.update("DELETE FROM orders WHERE user_id = ?", user.getId());
        jdbc.update("DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = ?)", user.getId());
        jdbc.update("DELETE FROM carts WHERE user_id = ?", user.getId());
        jdbc.update("DELETE FROM products WHERE category_id = ?", category.getId());
        jdbc.update("DELETE FROM categories WHERE id = ?", category.getId());
        jdbc.update("DELETE FROM users WHERE id = ?", user.getId());
    }

    private int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getStock();
    }

    private int count(String sql) {
        Integer total = jdbc.queryForObject(sql, Integer.class, user.getId());
        return total == null ? 0 : total;
    }

    @Test
    void checkout_shouldCommitOrderDecreaseStockAndEmptyCart() {
        Cart cart = new Cart(user);
        cart.addItem(mouse, 2);
        cart.addItem(keyboard, 1);
        cartRepository.save(cart);

        OrderResponse response = useCase.execute(user.getId());

        assertNotNull(response.id());
        assertEquals(OrderStatus.PAID, response.status());
        assertEquals(0, new BigDecimal("220.50").compareTo(response.totalAmount()));

        assertEquals(8, stockOf(mouse));
        assertEquals(9, stockOf(keyboard));
        assertEquals(1, count("SELECT count(*) FROM orders WHERE user_id = ?"));
        assertEquals(2, count("SELECT count(*) FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)"));
        assertEquals(0, count("SELECT count(*) FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = ?)"));
    }

    @Test
    void checkout_shouldRollbackEverythingWhenStockRunsOut() {
        Cart cart = new Cart(user);
        cart.addItem(mouse, 1);
        cart.addItem(keyboard, 5);
        cartRepository.save(cart);

        // depois de entrar no carrinho, outro comprador leva quase todo o estoque do teclado
        assertEquals(1, jdbc.update("UPDATE products SET stock = stock - 8 WHERE id = ?", keyboard.getId()));

        assertThrows(BusinessRuleException.class, () -> useCase.execute(user.getId()));

        // nada pode ter ficado salvo pela metade
        assertEquals(10, stockOf(mouse));
        assertEquals(2, stockOf(keyboard));
        assertEquals(0, count("SELECT count(*) FROM orders WHERE user_id = ?"));
        assertEquals(2, count("SELECT count(*) FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = ?)"));
    }
}
