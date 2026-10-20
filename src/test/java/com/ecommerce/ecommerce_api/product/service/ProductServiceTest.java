package com.ecommerce.ecommerce_api.product.service;

import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.exception.OptimisticLockingException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_api.product.dto.CreateProductRequest;
import com.ecommerce.ecommerce_api.product.dto.ProductResponse;
import com.ecommerce.ecommerce_api.product.dto.UpdateProductRequest;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    private final Category category = new Category("ELETRONICOS");

    private Product newProduct() {
        return new Product("Mouse", "Mouse sem fio", new BigDecimal("59.90"), 10, category);
    }

    private CreateProductRequest newCreateRequest(UUID categoryId) {
        return new CreateProductRequest("Mouse", "Mouse sem fio", new BigDecimal("59.90"), 10, categoryId);
    }

    private UpdateProductRequest newUpdateRequest(UUID categoryId, long version) {
        return new UpdateProductRequest("Notebook", "Notebook DELL 13° geração", new BigDecimal("1500"), 5, categoryId, version);
    }

    @Test
    void create_shouldCreateSuccessfully() {
        UUID categoryId = UUID.randomUUID();

        CreateProductRequest request = newCreateRequest(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse result = service.create(request);

        assertEquals("Mouse", result.name());
        assertEquals("Mouse sem fio", result.description());
        assertEquals(new BigDecimal("59.90"), result.price());
        assertEquals(10, result.stock());
        assertEquals("ELETRONICOS", result.categoryName());
        assertEquals(0, result.version());
    }

    @Test
    void create_shouldThrowResourceNotFoundWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        CreateProductRequest request = newCreateRequest(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        assertEquals("Category not found.", ex.getMessage());

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void list_shouldPaginateAllActiveProductsWhenSearchParamIsNull() {
        when(productRepository.findAllByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct(), newProduct())));

        var result = service.list(1, 2, null);

        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());

        verify(productRepository, never()).findByNameContainingIgnoreCaseAndActiveTrue(any(String.class), any(Pageable.class));

    }

    @Test
    void list_shouldPaginateAllActiveProductsWhenSearchParamIsEmpty() {
        when(productRepository.findAllByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct(), newProduct())));

        var result = service.list(1, 2, " ");

        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());

        verify(productRepository, never()).findByNameContainingIgnoreCaseAndActiveTrue(anyString(), any(Pageable.class));
    }

    @Test
    void list_shouldPaginateAllActiveProductsWithinPaginationLimits() {
        when(productRepository.findAllByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct(), newProduct())));

        var result = service.list(-1, 51, " ");

        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());

        var expectedPageable = PageRequest.of(0, 50, Sort.by("name").ascending());

        verify(productRepository).findAllByActiveTrue(expectedPageable);
    }

    @Test
    void list_shouldPaginateActiveProductsWithMatchingNameIgnoreCaseWhenSearchParamExists() {
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("mouse"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct(), newProduct())));

        var result = service.list(1, 2, " mouse ");

        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());

        verify(productRepository, never()).findAllByActiveTrue(any(Pageable.class));
    }

    @Test
    void list_shouldPaginateActiveProductsWithMatchingNameIgnoreCaseWithinPaginationLimits() {
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("mouse"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newProduct(), newProduct())));

        var result = service.list(-1, 51, " mouse ");

        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());

        var expectedPageable = PageRequest.of(0, 50, Sort.by("name").ascending());

        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue("mouse", expectedPageable);
    }

    @Test
    void getById_shouldGetProductWhenProductActiveExists() {
        UUID id = UUID.randomUUID();

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(newProduct()));

        var result = service.getById(id);

        assertEquals("Mouse", result.name());
        assertEquals("Mouse sem fio", result.description());
        assertEquals(new BigDecimal("59.90"), result.price());
        assertEquals(10, result.stock());
        assertEquals("ELETRONICOS", result.categoryName());
        assertEquals(0, result.version());
    }

    @Test
    void getById_shouldThrowResourceNotFoundWhenProductIsInactiveOrNotExists() {
        UUID id = UUID.randomUUID();

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.getById(id));

        assertEquals("Product not found.", ex.getMessage());
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        UUID id = UUID.randomUUID();
        Product product = newProduct();

        UUID newCategoryId = UUID.randomUUID();
        Category newCategory = new Category("TI");

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));
        when(productRepository.saveAndFlush(product)).thenAnswer(i -> i.getArgument(0));

        var result = service.update(id, newUpdateRequest(newCategoryId, 0));

        assertEquals("Notebook", result.name());
        assertEquals("Notebook DELL 13° geração", result.description());
        assertEquals(new BigDecimal("1500"), result.price());
        assertEquals(5, result.stock());
        assertEquals("TI", result.categoryName());
    }

    @Test
    void update_shouldThrowResourceNotFoundWhenProductIsInactiveOrNotExists() {
        UUID id = UUID.randomUUID();

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.update(id, newUpdateRequest(id, 0)));

        assertEquals("Product not found.", ex.getMessage());

        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void update_shouldThrowResourceNotFoundWhenCategoryNotExists() {
        UUID id = UUID.randomUUID();
        Product product = newProduct();

        UUID newCategoryId = UUID.randomUUID();

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.update(id, newUpdateRequest(newCategoryId, 0)));

        assertEquals("Category not found.", ex.getMessage());

        verify(productRepository).findByIdAndActiveTrue(id);
        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void update_shouldThrowOptimisticLockingExceptionWhenVersionDoesNotMatch() {
        UUID id = UUID.randomUUID();
        Product product = newProduct();

        UUID newCategoryId = UUID.randomUUID();
        Category newCategory = new Category("TI");

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));

        var ex = assertThrows(OptimisticLockingException.class, () -> service.update(id, newUpdateRequest(newCategoryId, 1)));

        assertEquals("Conflict detected: This product was modified by another user. Please refresh the page and try again.", ex.getMessage());

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void delete_shouldDeactivateProductInsteadOfRemovingIt() {
        Product product = newProduct();
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(product));

        service.delete(id);

        assertFalse(product.isActive());
    }

    @Test
    void delete_shouldFailWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        var ex = assertThrows(RuntimeException.class, () -> service.delete(id));

        assertEquals("Product not found.", ex.getMessage());
    }

    @Test
    void delete_shouldFailWhenProductIsAlreadyInactive() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));

        verify(productRepository, never()).save(any());
    }
}