package com.ecommerce.ecommerce_api.category.controller;

import com.ecommerce.ecommerce_api.category.dto.CategoryResponse;
import com.ecommerce.ecommerce_api.category.service.CategoryService;
import com.ecommerce.ecommerce_api.exception.CategoryAlreadyExistsException;
import com.ecommerce.ecommerce_api.security.JwtAuthenticationEntryPoint;
import com.ecommerce.ecommerce_api.security.JwtService;
import com.ecommerce.ecommerce_api.security.SecurityConfig;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
@WithMockUser
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void create_shouldReturn201() throws Exception {
        when(service.create(any())).thenReturn(new CategoryResponse(UUID.randomUUID(), "ELETRÔNICOS"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Eletrônicos\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ELETRÔNICOS"));
    }

    @Test
    void create_whenCategoryAlreadyExists_shouldReturn409() throws Exception {
        when(service.create(any())).thenThrow(new CategoryAlreadyExistsException("ELETRÔNICOS"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Eletrônicos\" }"))
                .andExpect(status().isConflict());
    }

    @Test
    void create_withBlankName_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"  \" }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithAnonymousUser
    void create_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Eletrônicos\" }"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }
}
