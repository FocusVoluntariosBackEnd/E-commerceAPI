package com.ecommerce.ecommerce_api.category.service;

import com.ecommerce.ecommerce_api.category.dto.CategoryResponse;
import com.ecommerce.ecommerce_api.category.dto.CreateCategoryRequest;
import com.ecommerce.ecommerce_api.category.entity.Category;
import com.ecommerce.ecommerce_api.category.repository.CategoryRepository;
import com.ecommerce.ecommerce_api.exception.CategoryAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        if (repository.findByName(request.name()).isPresent())
            throw new CategoryAlreadyExistsException(request.name());

        Category category;
        try {
            category = repository.saveAndFlush(new Category(request.name()));
        } catch (DataIntegrityViolationException ex) {
            // duas requisições simultâneas passaram pela checagem acima: a constraint UNIQUE do banco decide
            throw new CategoryAlreadyExistsException(request.name());
        }

        return new CategoryResponse(category.getId(), category.getName());
    }
}
