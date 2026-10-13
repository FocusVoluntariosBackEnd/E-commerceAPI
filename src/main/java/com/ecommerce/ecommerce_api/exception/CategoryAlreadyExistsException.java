package com.ecommerce.ecommerce_api.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String name) {
        super("Categoria já existe: " + name + ".");
    }
}
