package com.microservices.businessservice.controller;

import com.microservices.businessservice.dto.*;
import com.microservices.businessservice.service.ProductoBusinessService;
import com.microservices.businessservice.service.ReporteBusinessService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el microservicio de negocio
 * Maneja la lógica de negocio y se comunica con data-service vía Feign
 * 
 * @author Agustin Benavidez
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BusinessController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);

    private final ProductoBusinessService productoBusinessService;
    private final ReporteBusinessService reporteBusinessService;

    @Autowired
    public BusinessController(ProductoBusinessService productoBusinessService,
                             ReporteBusinessService reporteBusinessService) {
        this.productoBusinessService = productoBusinessService;
        this.reporteBusinessService = reporteBusinessService;
    }

    // ========== ENDPOINTS DE PRODUCTOS ==========

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        logger.info("GET /api/productos - Obteniendo todos los productos");
        List<ProductoDTO> productos = productoBusinessService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        logger.info("GET /api/productos/{} - Obteniendo producto por ID", id);
        ProductoDTO producto = productoBusinessService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoRequest request) {
        logger.info("POST /api/productos - Creando producto: {}", request.getNombre());
        ProductoDTO producto = productoBusinessService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id,
                                                         @Valid @RequestBody ProductoRequest request) {
        logger.info("PUT /api/productos/{} - Actualizando producto", id);
        ProductoDTO producto = productoBusinessService.actualizarProducto(id, request);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        logger.info("DELETE /api/productos/{} - Eliminando producto", id);
        productoBusinessService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/productos/categoria/{nombre}")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosPorCategoria(@PathVariable String nombre) {
        logger.info("GET /api/productos/categoria/{} - Obteniendo productos por categoría", nombre);
        List<ProductoDTO> productos = productoBusinessService.obtenerProductosPorCategoria(nombre);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/buscar")
    public ResponseEntity<List<ProductoDTO>> buscarProductos(@RequestParam String texto) {
        logger.info("GET /api/productos/buscar?texto={} - Buscando productos", texto);
        List<ProductoDTO> productos = productoBusinessService.buscarProductos(texto);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/precio")
    public ResponseEntity<List<ProductoDTO>> buscarProductosPorPrecio(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        logger.info("GET /api/productos/precio?min={}&max={} - Buscando productos por precio", min, max);
        List<ProductoDTO> productos = productoBusinessService.buscarProductosPorPrecio(min, max);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/stock-bajo")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosConStockBajo() {
        logger.info("GET /api/productos/stock-bajo - Obteniendo productos con stock bajo");
        List<ProductoDTO> productos = productoBusinessService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }

    // ========== ENDPOINTS DE REPORTES ==========

    @GetMapping("/reportes/inventario")
    public ResponseEntity<ReporteDTO> generarReporteInventario() {
        logger.info("GET /api/reportes/inventario - Generando reporte de inventario");
        ReporteDTO reporte = reporteBusinessService.generarReporteEstadoInventario();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/categorias")
    public ResponseEntity<ReporteDTO> generarReporteProductosPorCategoria() {
        logger.info("GET /api/reportes/categorias - Generando reporte por categorías");
        ReporteDTO reporte = reporteBusinessService.generarReporteProductosPorCategoria();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/alertas-stock")
    public ResponseEntity<ReporteDTO> generarReporteAlertasStock() {
        logger.info("GET /api/reportes/alertas-stock - Generando reporte de alertas");
        ReporteDTO reporte = reporteBusinessService.generarReporteAlertasStock();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/financiero")
    public ResponseEntity<ReporteDTO> generarReporteFinanciero() {
        logger.info("GET /api/reportes/financiero - Generando reporte financiero");
        ReporteDTO reporte = reporteBusinessService.generarReporteFinanciero();
        return ResponseEntity.ok(reporte);
    }

    // ========== ENDPOINTS DE MÉTRICAS DE NEGOCIO ==========

    @GetMapping("/metricas/valor-total-inventario")
    public ResponseEntity<Map<String, Object>> obtenerValorTotalInventario() {
        logger.info("GET /api/metricas/valor-total-inventario - Calculando valor total");
        BigDecimal valorTotal = productoBusinessService.calcularValorTotalInventario();
        
        Map<String, Object> response = Map.of(
            "valorTotal", valorTotal,
            "fechaCalculo", LocalDateTime.now(),
            "moneda", "ARS"
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metricas/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumenMetricas() {
        logger.info("GET /api/metricas/resumen - Obteniendo resumen de métricas");
        
        // Obtener datos básicos
        List<ProductoDTO> todosProductos = productoBusinessService.obtenerTodosLosProductos();
        List<ProductoDTO> stockBajo = productoBusinessService.obtenerProductosConStockBajo();
        BigDecimal valorTotal = productoBusinessService.calcularValorTotalInventario();
        
        // Calcular métricas
        int totalProductos = todosProductos.size();
        int productosActivos = (int) todosProductos.stream().filter(p -> Boolean.TRUE.equals(p.getActivo())).count();
        int productosStockBajo = stockBajo.size();
        
        Map<String, Object> resumen = Map.of(
            "totalProductos", totalProductos,
            "productosActivos", productosActivos,
            "productosConStockBajo", productosStockBajo,
            "valorTotalInventario", valorTotal,
            "porcentajeStockBajo", totalProductos > 0 ? (double) productosStockBajo / totalProductos * 100 : 0,
            "fechaGeneracion", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(resumen);
    }

    // ========== ENDPOINTS DE VALIDACIÓN Y UTILIDAD ==========

    @PostMapping("/productos/validar")
    public ResponseEntity<Map<String, Object>> validarProducto(@Valid @RequestBody ProductoRequest request) {
        logger.info("POST /api/productos/validar - Validando datos de producto");
        
        // Este endpoint solo valida sin crear el producto
        Map<String, Object> response = Map.of(
            "valido", true,
            "mensaje", "Los datos del producto son válidos",
            "producto", request,
            "fechaValidacion", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/productos/{id}/disponibilidad")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(@PathVariable Long id) {
        logger.info("GET /api/productos/{}/disponibilidad - Verificando disponibilidad", id);
        
        ProductoDTO producto = productoBusinessService.obtenerProductoPorId(id);
        
        boolean disponible = producto.isStockDisponible();
        String estado = producto.getEstadoStockCalculado();
        
        Map<String, Object> response = Map.of(
            "productoId", id,
            "nombreProducto", producto.getNombre(),
            "disponible", disponible,
            "stock", producto.getStock() != null ? producto.getStock() : 0,
            "stockMinimo", producto.getStockMinimo() != null ? producto.getStockMinimo() : 0,
            "estadoStock", estado,
            "fechaConsulta", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINT DE HEALTH CHECK ==========

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("GET /api/health - Health check del business service");
        
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "business-service",
            "version", "1.0.0",
            "timestamp", LocalDateTime.now(),
            "description", "Microservicio de negocio funcionando correctamente"
        );
        
        return ResponseEntity.ok(health);
    }

    // ========== ENDPOINT DE INFORMACIÓN DEL SERVICIO ==========

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> obtenerInformacionServicio() {
        logger.debug("GET /api/info - Información del servicio");
        
        Map<String, Object> info = Map.of(
            "nombre", "Business Service",
            "descripcion", "Microservicio de lógica de negocio para gestión de productos",
            "version", "1.0.0",
            "autor", "Agustin Benavidez",
            "legajo", "62344",
            "tecnologias", List.of("Spring Boot", "Spring Cloud OpenFeign", "Java 21"),
            "endpoints", Map.of(
                "productos", "/api/productos",
                "reportes", "/api/reportes",
                "metricas", "/api/metricas"
            ),
            "comunicacion", Map.of(
                "dataService", "Feign Client HTTP",
                "puerto", 8082,
                "fallbacks", "Habilitados"
            )
        );
        
        return ResponseEntity.ok(info);
    }
}
