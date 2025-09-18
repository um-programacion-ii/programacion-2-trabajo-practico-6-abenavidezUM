package com.microservices.businessservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para reportes de negocio
 * 
 * @author Agustin Benavidez
 */
public class ReporteDTO {

    private String tipoReporte;
    private String titulo;
    private String descripcion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaGeneracion;

    // Datos del reporte
    private Map<String, Object> datos;
    private List<ProductoDTO> productos;
    private List<CategoriaDTO> categorias;
    private List<InventarioDTO> inventarios;

    // Métricas generales
    private Integer totalProductos;
    private Integer productosActivos;
    private Integer productosConStockBajo;
    private Integer productosConStockCritico;
    private Integer productosSinStock;
    private BigDecimal valorTotalInventario;
    private BigDecimal valorPromedioProducto;

    // Métricas por categoría
    private Map<String, Integer> productosPorCategoria;
    private Map<String, BigDecimal> valorPorCategoria;
    private Map<String, Integer> stockBajoPorCategoria;

    // Constructor por defecto
    public ReporteDTO() {
        this.fechaGeneracion = LocalDateTime.now();
    }

    // Constructor básico
    public ReporteDTO(String tipoReporte, String titulo) {
        this();
        this.tipoReporte = tipoReporte;
        this.titulo = titulo;
    }

    // Constructor completo
    public ReporteDTO(String tipoReporte, String titulo, String descripcion, Map<String, Object> datos) {
        this(tipoReporte, titulo);
        this.descripcion = descripcion;
        this.datos = datos;
    }

    // Getters y Setters
    public String getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public void setDatos(Map<String, Object> datos) {
        this.datos = datos;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoDTO> productos) {
        this.productos = productos;
    }

    public List<CategoriaDTO> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaDTO> categorias) {
        this.categorias = categorias;
    }

    public List<InventarioDTO> getInventarios() {
        return inventarios;
    }

    public void setInventarios(List<InventarioDTO> inventarios) {
        this.inventarios = inventarios;
    }

    public Integer getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(Integer totalProductos) {
        this.totalProductos = totalProductos;
    }

    public Integer getProductosActivos() {
        return productosActivos;
    }

    public void setProductosActivos(Integer productosActivos) {
        this.productosActivos = productosActivos;
    }

    public Integer getProductosConStockBajo() {
        return productosConStockBajo;
    }

    public void setProductosConStockBajo(Integer productosConStockBajo) {
        this.productosConStockBajo = productosConStockBajo;
    }

    public Integer getProductosConStockCritico() {
        return productosConStockCritico;
    }

    public void setProductosConStockCritico(Integer productosConStockCritico) {
        this.productosConStockCritico = productosConStockCritico;
    }

    public Integer getProductosSinStock() {
        return productosSinStock;
    }

    public void setProductosSinStock(Integer productosSinStock) {
        this.productosSinStock = productosSinStock;
    }

    public BigDecimal getValorTotalInventario() {
        return valorTotalInventario;
    }

    public void setValorTotalInventario(BigDecimal valorTotalInventario) {
        this.valorTotalInventario = valorTotalInventario;
    }

    public BigDecimal getValorPromedioProducto() {
        return valorPromedioProducto;
    }

    public void setValorPromedioProducto(BigDecimal valorPromedioProducto) {
        this.valorPromedioProducto = valorPromedioProducto;
    }

    public Map<String, Integer> getProductosPorCategoria() {
        return productosPorCategoria;
    }

    public void setProductosPorCategoria(Map<String, Integer> productosPorCategoria) {
        this.productosPorCategoria = productosPorCategoria;
    }

    public Map<String, BigDecimal> getValorPorCategoria() {
        return valorPorCategoria;
    }

    public void setValorPorCategoria(Map<String, BigDecimal> valorPorCategoria) {
        this.valorPorCategoria = valorPorCategoria;
    }

    public Map<String, Integer> getStockBajoPorCategoria() {
        return stockBajoPorCategoria;
    }

    public void setStockBajoPorCategoria(Map<String, Integer> stockBajoPorCategoria) {
        this.stockBajoPorCategoria = stockBajoPorCategoria;
    }

    @Override
    public String toString() {
        return "ReporteDTO{" +
                "tipoReporte='" + tipoReporte + '\'' +
                ", titulo='" + titulo + '\'' +
                ", fechaGeneracion=" + fechaGeneracion +
                ", totalProductos=" + totalProductos +
                ", valorTotalInventario=" + valorTotalInventario +
                '}';
    }
}
