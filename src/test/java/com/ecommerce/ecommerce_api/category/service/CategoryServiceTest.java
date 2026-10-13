package com.ecommerce.ecommerce_api.category.service;

import com.ecommerce.ecommerce_api.category.dto.CategoryResponse;
import com.ecommerce.ecommerce_api.category.dto.CreateCategoryRequest;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.exception.CategoryAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    @Test
    void create_shouldSaveAndReturnCategory() {
        var request = new CreateCategoryRequest("eletrônicos");
        when(repository.findByName("ELETRÔNICOS")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = service.create(request);

        assertEquals("ELETRÔNICOS", response.name());
    }

    @Test
    void create_shouldFailWhenCategoryAlreadyExists() {
        var request = new CreateCategoryRequest("Eletrônicos");
        when(repository.findByName("ELETRÔNICOS")).thenReturn(Optional.of(new Category("ELETRÔNICOS")));

        assertThrows(CategoryAlreadyExistsException.class, () -> service.create(request));

        verify(repository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void create_shouldTranslateUniqueConstraintViolationIntoConflict() {
        var request = new CreateCategoryRequest("Eletrônicos");
        when(repository.findByName("ELETRÔNICOS")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Category.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(CategoryAlreadyExistsException.class, () -> service.create(request));
    }
}
