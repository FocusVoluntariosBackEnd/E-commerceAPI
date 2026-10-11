package com.ecommerce.ecommerce_api.product.service;

import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.product.dto.CreateProductRequest;
import com.ecommerce.ecommerce_api.product.dto.ProductResponse;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService service;

    private final Category category = new Category("Eletrônicos");

    private Product newProduct() {
        return new Product("Mouse", "Mouse sem fio", new BigDecimal("59.90"), 10, category);
    }

    @Test
    void create_shouldPersistAndReturnProduct() {
        UUID categoryId = UUID.randomUUID();
        var request = new CreateProductRequest("Mouse", "Mouse sem fio", new BigDecimal("59.90"), 10, categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.create(request);

        assertEquals("Mouse", response.name());
        assertEquals(10, response.stock());
        assertEquals("Eletrônicos", response.categoryName());
    }

    @Test
    void create_shouldFailWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();
        var request = new CreateProductRequest("Mouse", null, BigDecimal.TEN, 1, categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    void list_withoutSearch_shouldReturnOnlyActiveProducts() {
        when(productRepository.findAllByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct())));

        var result = service.list(1, 10, null);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByActiveTrue(any(Pageable.class));
    }

    @Test
    void list_withSearch_shouldTrimAndSearchActiveProducts() {
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("mouse"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct())));

        var result = service.list(1, 10, "  mouse  ");

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue(eq("mouse"), any(Pageable.class));
    }

    @Test
    void delete_shouldDeactivateProductInsteadOfRemovingIt() {
        Product product = newProduct();
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        service.delete(id);

        assertFalse(product.isActive());
        verify(productRepository, never()).delete(any());
    }

    @Test
    void delete_shouldFailWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.delete(id));
    }
}