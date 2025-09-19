package com.microservices.dataservice.exception;

/**
 * Excepción lanzada cuando no hay suficiente stock para una operación
 * 
 * @author Agustin Benavidez
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productoId;
    private final Integer stockDisponible;
    private final Integer stockRequerido;

    public InsufficientStockException(Long productoId, Integer stockDisponible, Integer stockRequerido) {
        super(String.format("Stock insuficiente para producto ID %d. Disponible: %d, Requerido: %d", 
              productoId, stockDisponible, stockRequerido));
        this.productoId = productoId;
        this.stockDisponible = stockDisponible;
        this.stockRequerido = stockRequerido;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.productoId = null;
        this.stockDisponible = null;
        this.stockRequerido = null;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public Integer getStockRequerido() {
        return stockRequerido;
    }
}
