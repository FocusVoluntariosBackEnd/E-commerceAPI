package com.ecommerce.ecommerce_api.product.controller;

import com.ecommerce.ecommerce_api.product.dto.ProductResponse;
import com.ecommerce.ecommerce_api.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.ecommerce.ecommerce_api.security.SecurityConfig;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    private static final String CATEGORY_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    private String body(String name, String price, String stock, String categoryId) {
        return """
                {
                  "name": %s,
                  "description": "desc",
                  "price": %s,
                  "stock": %s,
                  "categoryId": %s
                }
                """.formatted(name, price, stock, categoryId);
    }

    private ProductResponse sampleResponse() {
        return new ProductResponse(UUID.randomUUID(), "Mouse", "desc", new BigDecimal("59.90"),
                10, "Eletrônicos", Instant.now(), Instant.now());
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"Mouse\"", "59.90", "10", "\"" + CATEGORY_ID + "\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mouse"));
    }

    @Test
    void create_withBlankName_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"  \"", "59.90", "10", "\"" + CATEGORY_ID + "\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNegativePrice_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"Mouse\"", "-1", "10", "\"" + CATEGORY_ID + "\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNegativeStock_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"Mouse\"", "59.90", "-5", "\"" + CATEGORY_ID + "\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withMissingCategoryId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"Mouse\"", "59.90", "10", "null")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_shouldReturn200() throws Exception {
        when(service.list(1, 10, null)).thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Mouse"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}