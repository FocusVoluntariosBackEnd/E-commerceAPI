package com.ecommerce.ecommerce_api.product.entity;

import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.exception.OptimisticLockingException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("ELETRONICOS");
        product = new Product(
                "Notebook",
                "Notebook DELL 13° geração",
                new BigDecimal("3500"),
                10,
                category
        );
    }

    @Test
    void shouldCreateProductWithActiveTrueAndVersionZero() {
        assertTrue(product.isActive());
        assertEquals(0, product.getVersion());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 10})
    void shouldReturnTrueWhenQuantityLessThanOrEquals(int quantity) {
        assertTrue(product.isStockAvailable(quantity));
    }

    @Test
    void shouldReturnFalseWhenQuantityMoreThan() {
        assertFalse(product.isStockAvailable(15));
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        Category updatedCategory = new Category("ELETRONICOS_AUXILIARES");

        product.updateProduct(
                "Mouse",
                "Mouse LED RGB",
                new BigDecimal("50"),
                15,
                updatedCategory,
                product.getVersion()
        );

        assertEquals("Mouse", product.getName());
        assertEquals("Mouse LED RGB", product.getDescription());
        assertEquals(new BigDecimal("50"), product.getPrice());
        assertEquals(15, product.getStock());
        assertEquals(updatedCategory, product.getCategory());
    }

    @Test
    void shouldThrowResourceNotFoundWhenProductIsInactive() {
        product.deleteProduct();

        Category updatedCategory = new Category("ELETRONICOS_AUXILIARES");

        assertThrows(
                ResourceNotFoundException.class,
                () -> product.updateProduct(
                        "Mouse",
                        "Mouse LED RGB",
                        new BigDecimal("50"),
                        15,
                        updatedCategory,
                        product.getVersion()
                )
        );
    }

    @Test
    void shouldThrowOptimistLockingExceptionWhenRequestVersionNonEquals() {
        Category updatedCategory = new Category("ELETRONICOS_AUXILIARES");

        assertThrows(
                OptimisticLockingException.class,
                () -> product.updateProduct(
                        "Notebook",
                        "Notebook DELL 13° geração",
                        new BigDecimal("3500"),
                        10,
                        updatedCategory,
                        product.getVersion() + 1
                )
        );
    }

    @Test
    void shouldUpdatesActiveFalseWhenProductIsDeleted() {
        product.deleteProduct();

        assertFalse(product.isActive());
    }
}