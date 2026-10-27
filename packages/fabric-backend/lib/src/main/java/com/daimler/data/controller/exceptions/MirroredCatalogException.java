package com.daimler.data.controller.exceptions;

import org.springframework.http.HttpStatus;

public class MirroredCatalogException extends RuntimeException {

    private final String ddxCorrelationId;
    private final String errorCode;
    private final HttpStatus httpStatus;

    public MirroredCatalogException(String message, String ddxCorrelationId, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.ddxCorrelationId = ddxCorrelationId;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getDdxCorrelationId() {
        return ddxCorrelationId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
