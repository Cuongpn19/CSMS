package com.csms.exception;

public class InsufficientIngredientException
        extends IllegalStateException {

    public InsufficientIngredientException(
            String message) {
        super(message);
    }
}