package com.ecommerce.ecommerce_api.order.controller;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import com.ecommerce.ecommerce_api.cart.repository.CartRepository;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import com.ecommerce.ecommerce_api.security.JwtService;
import com.ecommerce.ecommerce_api.user.entity.User;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "api.security.jwt.secret=chave_de_teste_com_mais_de_32_caracteres_ok",
        "api.security.jwt.expiration-millis=3600000"
})
class CheckoutControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String token;
    private User user;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        String tag = UUID.randomUUID().toString().substring(0, 8);

        user = userRepository.save(
                new User("Teste", "checkout_" + tag + "@test.com",
                        passwordEncoder.encode("12345678")));

        category = categoryRepository.save(new Category("Cat-" + tag));

        product = productRepository.save(
                new Product("Notebook-" + tag, "Notebook DELL i5 13° geração", new BigDecimal("3500.00"), 5, category));

        Cart cart = cartRepository.save(new Cart(user));
        cart.addItem(product, 2);
        cartRepository.save(cart);

        token = "Bearer " + jwtService.generateToken(user);
    }

    @Test
    void shouldProcessCheckoutSuccessfully() throws Exception {
        mockMvc.perform(post("/api/checkout")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.totalAmount").value(7000.00))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/checkout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn409WhenCartIsEmpty() throws Exception {
        Cart cart = cartRepository.findByUserIdWithItems(user.getId()).orElseThrow();
        cart.clear();
        cartRepository.save(cart);

        mockMvc.perform(post("/api/checkout")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Can't checkout with cart is empty."));
    }

    @Test
    void shouldReturn409WhenProductHasInsufficientStock() throws Exception {
        product.updateProduct(product.getName(), product.getDescription(),
                product.getPrice(), 0, category, product.getVersion());
        productRepository.save(product);

        mockMvc.perform(post("/api/checkout")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Insufficient stock for product " + product.getName() + "."));
    }
}