package com.daimler.data.controller.exceptions;



public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " not found with identifier: " + identifier);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
