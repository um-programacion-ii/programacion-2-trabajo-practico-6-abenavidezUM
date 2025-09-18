package com.microservices.businessservice.exception;

/**
 * Excepción para cuando un servicio externo no está disponible
 * 
 * @author Agustin Benavidez
 */
public class ServiceUnavailableException extends BusinessException {

    private final String serviceName;
    private final String operation;

    public ServiceUnavailableException(String serviceName, String operation) {
        super(String.format("Servicio '%s' no disponible para la operación '%s'", serviceName, operation));
        this.serviceName = serviceName;
        this.operation = operation;
    }

    public ServiceUnavailableException(String serviceName, String operation, Throwable cause) {
        super(String.format("Servicio '%s' no disponible para la operación '%s'", serviceName, operation), cause);
        this.serviceName = serviceName;
        this.operation = operation;
    }

    public ServiceUnavailableException(String message, String serviceName, String operation) {
        super(message);
        this.serviceName = serviceName;
        this.operation = operation;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getOperation() {
        return operation;
    }
}
