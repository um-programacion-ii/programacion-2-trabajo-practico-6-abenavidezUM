package com.microservices.businessservice.client;

import com.microservices.businessservice.dto.CategoriaDTO;
import com.microservices.businessservice.dto.InventarioDTO;
import com.microservices.businessservice.dto.ProductoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de fallback para el cliente Feign del data-service
 * Se ejecuta cuando el microservicio de datos no está disponible
 * 
 * @author Agustin Benavidez
 */
@Component
public class DataServiceClientFallback implements DataServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DataServiceClientFallback.class);

    private void logFallback(String metodo) {
        logger.warn("FALLBACK: Ejecutando fallback para {} - Data service no disponible", metodo);
    }

    // ========== ENDPOINTS DE PRODUCTOS ==========

    @Override
    public List<ProductoDTO> obtenerTodosLosProductos() {
        logFallback("obtenerTodosLosProductos");
        return Collections.emptyList();
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Long id) {
        logFallback("obtenerProductoPorId");
        return null;
    }

    @Override
    public ProductoDTO crearProducto(ProductoDTO producto, Integer cantidadInicial, Integer stockMinimo) {
        logFallback("crearProducto");
        return null;
    }

    @Override
    public ProductoDTO actualizarProducto(Long id, ProductoDTO producto) {
        logFallback("actualizarProducto");
        return null;
    }

    @Override
    public void eliminarProducto(Long id) {
        logFallback("eliminarProducto");
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorCategoria(String categoriaNombre) {
        logFallback("obtenerProductosPorCategoria");
        return Collections.emptyList();
    }

    @Override
    public List<ProductoDTO> buscarProductos(String texto) {
        logFallback("buscarProductos");
        return Collections.emptyList();
    }

    @Override
    public List<ProductoDTO> buscarProductosPorPrecio(BigDecimal min, BigDecimal max) {
        logFallback("buscarProductosPorPrecio");
        return Collections.emptyList();
    }

    @Override
    public List<ProductoDTO> obtenerProductosConStockBajo() {
        logFallback("obtenerProductosConStockBajo");
        return Collections.emptyList();
    }

    @Override
    public List<ProductoDTO> obtenerProductosSinStock() {
        logFallback("obtenerProductosSinStock");
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> obtenerValorInventarioPorProducto() {
        logFallback("obtenerValorInventarioPorProducto");
        return Collections.emptyList();
    }

    @Override
    public ProductoDTO reactivarProducto(Long id) {
        logFallback("reactivarProducto");
        return null;
    }

    // ========== ENDPOINTS DE CATEGORÍAS ==========

    @Override
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        logFallback("obtenerTodasLasCategorias");
        return Collections.emptyList();
    }

    @Override
    public CategoriaDTO obtenerCategoriaPorId(Long id) {
        logFallback("obtenerCategoriaPorId");
        return null;
    }

    @Override
    public CategoriaDTO obtenerCategoriaPorNombre(String nombre) {
        logFallback("obtenerCategoriaPorNombre");
        return null;
    }

    @Override
    public CategoriaDTO crearCategoria(CategoriaDTO categoria) {
        logFallback("crearCategoria");
        return null;
    }

    @Override
    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoria) {
        logFallback("actualizarCategoria");
        return null;
    }

    @Override
    public void eliminarCategoria(Long id) {
        logFallback("eliminarCategoria");
    }

    @Override
    public List<CategoriaDTO> buscarCategorias(String texto) {
        logFallback("buscarCategorias");
        return Collections.emptyList();
    }

    @Override
    public List<CategoriaDTO> obtenerCategoriasConProductos() {
        logFallback("obtenerCategoriasConProductos");
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> obtenerEstadisticasCategorias() {
        logFallback("obtenerEstadisticasCategorias");
        return Collections.emptyList();
    }

    // ========== ENDPOINTS DE INVENTARIO ==========

    @Override
    public List<InventarioDTO> obtenerTodoElInventario() {
        logFallback("obtenerTodoElInventario");
        return Collections.emptyList();
    }

    @Override
    public InventarioDTO obtenerInventarioPorId(Long id) {
        logFallback("obtenerInventarioPorId");
        return null;
    }

    @Override
    public InventarioDTO obtenerInventarioPorProducto(Long productoId) {
        logFallback("obtenerInventarioPorProducto");
        return null;
    }

    @Override
    public InventarioDTO crearInventario(InventarioDTO inventario) {
        logFallback("crearInventario");
        return null;
    }

    @Override
    public InventarioDTO actualizarInventario(Long id, InventarioDTO inventario) {
        logFallback("actualizarInventario");
        return null;
    }

    @Override
    public InventarioDTO actualizarStock(Long productoId, Integer cantidad) {
        logFallback("actualizarStock");
        return null;
    }

    @Override
    public InventarioDTO incrementarStock(Long productoId, Integer incremento) {
        logFallback("incrementarStock");
        return null;
    }

    @Override
    public InventarioDTO decrementarStock(Long productoId, Integer decremento) {
        logFallback("decrementarStock");
        return null;
    }

    @Override
    public List<InventarioDTO> obtenerInventariosConStockBajo() {
        logFallback("obtenerInventariosConStockBajo");
        return Collections.emptyList();
    }

    @Override
    public List<InventarioDTO> obtenerInventariosConStockCritico() {
        logFallback("obtenerInventariosConStockCritico");
        return Collections.emptyList();
    }

    @Override
    public List<InventarioDTO> obtenerInventariosSinStock() {
        logFallback("obtenerInventariosSinStock");
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> obtenerEstadisticasInventario() {
        logFallback("obtenerEstadisticasInventario");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", 0);
        stats.put("cantidadTotalItems", 0);
        stats.put("cantidadPromedio", 0.0);
        stats.put("productosConStockBajo", 0);
        stats.put("servicioDisponible", false);
        return stats;
    }

    @Override
    public BigDecimal obtenerValorTotalInventario() {
        logFallback("obtenerValorTotalInventario");
        return BigDecimal.ZERO;
    }

    @Override
    public List<InventarioDTO> obtenerInventarioPorCategoria(String categoriaNombre) {
        logFallback("obtenerInventarioPorCategoria");
        return Collections.emptyList();
    }

    @Override
    public List<InventarioDTO> obtenerInventariosParaReabastecimiento() {
        logFallback("obtenerInventariosParaReabastecimiento");
        return Collections.emptyList();
    }

    @Override
    public List<InventarioDTO> obtenerInventariosActualizadosRecientes(int dias) {
        logFallback("obtenerInventariosActualizadosRecientes");
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> healthCheck() {
        logFallback("healthCheck");
        Map<String, Object> health = new HashMap<>();
        health.put("status", "DOWN");
        health.put("service", "data-service");
        health.put("message", "Servicio no disponible - usando fallback");
        return health;
    }
}
