package com.microservices.businessservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para transferencia de datos de Categoria entre microservicios
 * 
 * @author Agustin Benavidez
 */
public class CategoriaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;

    // Campos adicionales para estadísticas
    private Integer cantidadProductos;
    private Integer cantidadProductosActivos;
    private java.math.BigDecimal valorTotalInventario;

    // Constructor por defecto
    public CategoriaDTO() {
    }

    // Constructor básico
    public CategoriaDTO(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Constructor completo
    public CategoriaDTO(Long id, String nombre, String descripcion, Integer cantidadProductos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadProductos = cantidadProductos;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(Integer cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public Integer getCantidadProductosActivos() {
        return cantidadProductosActivos;
    }

    public void setCantidadProductosActivos(Integer cantidadProductosActivos) {
        this.cantidadProductosActivos = cantidadProductosActivos;
    }

    public java.math.BigDecimal getValorTotalInventario() {
        return valorTotalInventario;
    }

    public void setValorTotalInventario(java.math.BigDecimal valorTotalInventario) {
        this.valorTotalInventario = valorTotalInventario;
    }

    // Métodos de conveniencia
    public boolean tieneProductos() {
        return cantidadProductos != null && cantidadProductos > 0;
    }

    public boolean tieneProductosActivos() {
        return cantidadProductosActivos != null && cantidadProductosActivos > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoriaDTO that = (CategoriaDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public String toString() {
        return "CategoriaDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", cantidadProductos=" + cantidadProductos +
                ", valorTotalInventario=" + valorTotalInventario +
                '}';
    }
}
