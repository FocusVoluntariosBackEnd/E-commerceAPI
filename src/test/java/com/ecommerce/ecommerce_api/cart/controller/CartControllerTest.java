package com.ecommerce.ecommerce_api.cart.controller;

import com.ecommerce.ecommerce_api.cart.dto.CartItemResponse;
import com.ecommerce.ecommerce_api.cart.dto.CartResponse;
import com.ecommerce.ecommerce_api.cart.service.CartService;
import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import com.ecommerce.ecommerce_api.security.JwtService;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(CartControllerTest.TestSecurityConfig.class)
class CartControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .build();
        }
    }

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CartService service;

    private RequestPostProcessor loggedUser() {
        return authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of()));
    }

    private CartResponse cartWith(int quantity) {
        BigDecimal unitPrice = new BigDecimal("50.00");
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return new CartResponse(List.of(new CartItemResponse(PRODUCT_ID, "Mouse", quantity, unitPrice, subtotal)), subtotal);
    }

    private String addBody(String productId, String quantity) {
        return """
                { "productId": %s, "quantity": %s }
                """.formatted(productId, quantity);
    }

    @Test
    void addItem_withValidBody_shouldReturn201AndCart() throws Exception {
        when(service.addItem(eq(USER_ID), any())).thenReturn(cartWith(2));

        mockMvc.perform(post("/api/cart/items").with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("\"" + PRODUCT_ID + "\"", "2")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].productId").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(100.00))
                .andExpect(jsonPath("$.total").value(100.00));
    }

    @Test
    void addItem_withZeroQuantity_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/cart/items").with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("\"" + PRODUCT_ID + "\"", "0")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void addItem_withNegativeQuantity_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/cart/items").with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("\"" + PRODUCT_ID + "\"", "-1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void addItem_withMissingProductId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/cart/items").with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("null", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void addItem_withInsufficientStock_shouldReturn409() throws Exception {
        when(service.addItem(eq(USER_ID), any()))
                .thenThrow(new BusinessRuleException("Insufficient stock to add to cart."));

        mockMvc.perform(post("/api/cart/items").with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("\"" + PRODUCT_ID + "\"", "999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Insufficient stock to add to cart."));
    }

    @Test
    void addItem_withUnknownProduct_shouldReturn404() throws Exception {
        when(service.addItem(eq(USER_ID), any()))
                .thenThrow(new ResourceNotFoundException("Product not found."));

        mockMvc.perform(post("/api/cart/items").with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("\"" + PRODUCT_ID + "\"", "1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItem_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody("\"" + PRODUCT_ID + "\"", "1")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getCart_shouldReturn200WithItemsAndTotal() throws Exception {
        when(service.getCart(USER_ID)).thenReturn(cartWith(1));

        mockMvc.perform(get("/api/cart").with(loggedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Mouse"))
                .andExpect(jsonPath("$.total").value(50.00));
    }

    @Test
    void updateQuantity_withValidBody_shouldReturn200() throws Exception {
        when(service.updateQuantityCartItem(eq(USER_ID), eq(PRODUCT_ID), any())).thenReturn(cartWith(3));

        mockMvc.perform(put("/api/cart/items/{productId}", PRODUCT_ID).with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": 3 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.total").value(150.00));
    }

    @Test
    void updateQuantity_withZeroQuantity_shouldReturn400() throws Exception {
        mockMvc.perform(put("/api/cart/items/{productId}", PRODUCT_ID).with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": 0 }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void updateQuantity_aboveStock_shouldReturn409() throws Exception {
        when(service.updateQuantityCartItem(eq(USER_ID), eq(PRODUCT_ID), any()))
                .thenThrow(new BusinessRuleException("Insufficient stock to add to cart."));

        mockMvc.perform(put("/api/cart/items/{productId}", PRODUCT_ID).with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": 999 }"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateQuantity_whenItemNotInCart_shouldReturn404() throws Exception {
        when(service.updateQuantityCartItem(eq(USER_ID), eq(PRODUCT_ID), any()))
                .thenThrow(new ResourceNotFoundException("Cart item not found."));

        mockMvc.perform(put("/api/cart/items/{productId}", PRODUCT_ID).with(loggedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": 1 }"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItem_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{productId}", PRODUCT_ID).with(loggedUser()))
                .andExpect(status().isNoContent());

        verify(service).removeItem(USER_ID, PRODUCT_ID);
    }

    @Test
    void removeItem_whenItemNotInCart_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Cart item not found."))
                .when(service).removeItem(USER_ID, PRODUCT_ID);

        mockMvc.perform(delete("/api/cart/items/{productId}", PRODUCT_ID).with(loggedUser()))
                .andExpect(status().isNotFound());
    }
}