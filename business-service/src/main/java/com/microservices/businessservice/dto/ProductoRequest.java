package com.microservices.businessservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO para requests de creación y actualización de productos
 * 
 * @author Agustin Benavidez
 */
public class ProductoRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @Min(value = 0, message = "El stock inicial no puede ser negativo")
    private Integer stockInicial;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    // Constructor por defecto
    public ProductoRequest() {
    }

    // Constructor completo
    public ProductoRequest(String nombre, String descripcion, BigDecimal precio, 
                          Long categoriaId, Integer stockInicial, Integer stockMinimo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoriaId = categoriaId;
        this.stockInicial = stockInicial;
        this.stockMinimo = stockMinimo;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Integer getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(Integer stockInicial) {
        this.stockInicial = stockInicial;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    @Override
    public String toString() {
        return "ProductoRequest{" +
                "nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", categoriaId=" + categoriaId +
                ", stockInicial=" + stockInicial +
                ", stockMinimo=" + stockMinimo +
                '}';
    }
}
