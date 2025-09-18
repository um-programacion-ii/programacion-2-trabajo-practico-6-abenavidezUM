package com.microservices.businessservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para transferencia de datos de Inventario entre microservicios
 * 
 * @author Agustin Benavidez
 */
public class InventarioDTO {

    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    private String productoNombre;
    private BigDecimal productoPrecio;
    private String categoriaNombre;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUltimaActualizacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    // Campos calculados
    private Boolean stockBajo;
    private Boolean stockCritico;
    private String estadoStock;
    private BigDecimal valorTotal;
    private Integer diasSinActualizacion;

    // Constructor por defecto
    public InventarioDTO() {
    }

    // Constructor básico
    public InventarioDTO(Long productoId, Integer cantidad, Integer stockMinimo) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    // Constructor completo
    public InventarioDTO(Long id, Long productoId, String productoNombre, 
                        Integer cantidad, Integer stockMinimo, Boolean stockBajo) {
        this.id = id;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
        this.stockBajo = stockBajo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public BigDecimal getProductoPrecio() {
        return productoPrecio;
    }

    public void setProductoPrecio(BigDecimal productoPrecio) {
        this.productoPrecio = productoPrecio;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(Boolean stockBajo) {
        this.stockBajo = stockBajo;
    }

    public Boolean getStockCritico() {
        return stockCritico;
    }

    public void setStockCritico(Boolean stockCritico) {
        this.stockCritico = stockCritico;
    }

    public String getEstadoStock() {
        return estadoStock;
    }

    public void setEstadoStock(String estadoStock) {
        this.estadoStock = estadoStock;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Integer getDiasSinActualizacion() {
        return diasSinActualizacion;
    }

    public void setDiasSinActualizacion(Integer diasSinActualizacion) {
        this.diasSinActualizacion = diasSinActualizacion;
    }

    // Métodos de negocio
    public boolean isStockBajo() {
        if (cantidad == null || stockMinimo == null) return false;
        return cantidad <= stockMinimo;
    }

    public boolean isStockCritico() {
        if (cantidad == null || stockMinimo == null) return false;
        return cantidad <= (stockMinimo * 0.5);
    }

    public boolean isDisponible() {
        return cantidad != null && cantidad > 0;
    }

    public String calcularEstadoStock() {
        if (!isDisponible()) return "SIN_STOCK";
        if (isStockCritico()) return "CRITICO";
        if (isStockBajo()) return "BAJO";
        return "NORMAL";
    }

    public BigDecimal calcularValorTotal() {
        if (productoPrecio == null || cantidad == null) return BigDecimal.ZERO;
        return productoPrecio.multiply(BigDecimal.valueOf(cantidad));
    }

    public boolean necesitaReabastecimiento() {
        return !isDisponible() || isStockCritico();
    }

    public Integer calcularDiferenciaStockMinimo() {
        if (cantidad == null || stockMinimo == null) return null;
        return cantidad - stockMinimo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioDTO that = (InventarioDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productoId);
    }

    @Override
    public String toString() {
        return "InventarioDTO{" +
                "id=" + id +
                ", productoNombre='" + productoNombre + '\'' +
                ", cantidad=" + cantidad +
                ", stockMinimo=" + stockMinimo +
                ", estadoStock='" + calcularEstadoStock() + '\'' +
                ", valorTotal=" + calcularValorTotal() +
                '}';
    }
}
