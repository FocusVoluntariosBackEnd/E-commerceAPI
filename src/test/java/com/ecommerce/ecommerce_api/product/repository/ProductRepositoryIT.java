package com.ecommerce.ecommerce_api.product.repository;

import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final Pageable pageable = PageRequest.of(0, 10);
    private Category category;
    private String tag;

    @BeforeEach
    void setUp() {
        tag = UUID.randomUUID().toString().substring(0, 8);
        category = categoryRepository.saveAndFlush(new Category("Cat-" + tag));
    }

    private Product save(String name) {
        return productRepository.saveAndFlush(
                new Product(name, "desc", new BigDecimal("10.00"), 5, category));
    }

    @Test
    void search_shouldIgnoreCaseAndMatchPartialName() {
        save("Mouse-" + tag);

        Page<Product> page = productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(tag.toUpperCase(), pageable);

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void search_shouldNotReturnInactiveProducts() {
        save("Mouse-" + tag);
        Product removed = save("Teclado-" + tag);
        removed.deleteProduct();
        productRepository.saveAndFlush(removed);

        Page<Product> page = productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(tag, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Mouse-" + tag, page.getContent().get(0).getName());
    }
}