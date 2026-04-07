package com.daimler.data.controller.exceptions;

public class OpenMetadataClientException extends RuntimeException {
    public OpenMetadataClientException(String message) {
        super(message);
    }
    
    public OpenMetadataClientException(String message, Throwable cause) {
        super(message, cause);
    }
}