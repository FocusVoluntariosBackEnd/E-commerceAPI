package com.ecommerce.ecommerce_api.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("E-mail já está em uso.");
    }
}