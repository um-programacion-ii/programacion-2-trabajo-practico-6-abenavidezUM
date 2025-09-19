package com.microservices.businessservice.exception;

import java.util.List;
import java.util.Map;

/**
 * Excepción para errores de validación de negocio
 * 
 * @author Agustin Benavidez
 */
public class ValidationException extends BusinessException {

    private final Map<String, String> fieldErrors;
    private final List<String> globalErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = null;
        this.globalErrors = null;
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
        this.globalErrors = null;
    }

    public ValidationException(String message, List<String> globalErrors) {
        super(message);
        this.fieldErrors = null;
        this.globalErrors = globalErrors;
    }

    public ValidationException(String message, Map<String, String> fieldErrors, List<String> globalErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
        this.globalErrors = globalErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public List<String> getGlobalErrors() {
        return globalErrors;
    }

    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    public boolean hasGlobalErrors() {
        return globalErrors != null && !globalErrors.isEmpty();
    }
}
