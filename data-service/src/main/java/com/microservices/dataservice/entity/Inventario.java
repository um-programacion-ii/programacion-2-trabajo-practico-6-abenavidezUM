package com.microservices.dataservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa el inventario de un producto
 * 
 * @author Agustin Benavidez
 */
@Entity
@Table(name = "inventario",
       indexes = {
           @Index(name = "idx_inventario_producto", columnList = "producto_id"),
           @Index(name = "idx_inventario_cantidad", columnList = "cantidad"),
           @Index(name = "idx_inventario_stock_minimo", columnList = "stock_minimo")
       })
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El producto es obligatorio")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Version
    private Long version;

    // Constructor por defecto
    public Inventario() {
    }

    // Constructor con parámetros
    public Inventario(Producto producto, Integer cantidad, Integer stockMinimo) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaUltimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaUltimaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Métodos de conveniencia
    public boolean isStockBajo() {
        return cantidad <= stockMinimo;
    }

    public boolean isStockCritico() {
        return cantidad <= (stockMinimo * 0.5);
    }

    public boolean isDisponible() {
        return cantidad > 0;
    }

    public String getProductoNombre() {
        return producto != null ? producto.getNombre() : null;
    }

    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
    }

    /**
     * Actualiza la cantidad del inventario
     * @param nuevaCantidad la nueva cantidad
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarCantidad(Integer nuevaCantidad) {
        if (nuevaCantidad == null || nuevaCantidad < 0) {
            return false;
        }
        this.cantidad = nuevaCantidad;
        this.fechaUltimaActualizacion = LocalDateTime.now();
        return true;
    }

    /**
     * Incrementa la cantidad del inventario
     * @param incremento cantidad a incrementar
     * @return true si el incremento fue exitoso
     */
    public boolean incrementarStock(Integer incremento) {
        if (incremento == null || incremento <= 0) {
            return false;
        }
        this.cantidad += incremento;
        this.fechaUltimaActualizacion = LocalDateTime.now();
        return true;
    }

    /**
     * Decrementa la cantidad del inventario
     * @param decremento cantidad a decrementar
     * @return true si el decremento fue exitoso
     */
    public boolean decrementarStock(Integer decremento) {
        if (decremento == null || decremento <= 0 || decremento > cantidad) {
            return false;
        }
        this.cantidad -= decremento;
        this.fechaUltimaActualizacion = LocalDateTime.now();
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventario that = (Inventario) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(producto, that.producto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, producto);
    }

    @Override
    public String toString() {
        return "Inventario{" +
                "id=" + id +
                ", producto=" + getProductoNombre() +
                ", cantidad=" + cantidad +
                ", stockMinimo=" + stockMinimo +
                ", stockBajo=" + isStockBajo() +
                ", fechaUltimaActualizacion=" + fechaUltimaActualizacion +
                '}';
    }
}
