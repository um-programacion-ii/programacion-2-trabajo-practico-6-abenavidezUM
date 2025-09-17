package com.microservices.dataservice.controller;

import com.microservices.dataservice.entity.Categoria;
import com.microservices.dataservice.entity.Inventario;
import com.microservices.dataservice.entity.Producto;
import com.microservices.dataservice.service.CategoriaService;
import com.microservices.dataservice.service.InventarioService;
import com.microservices.dataservice.service.ProductoService;
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
 * Controlador REST para el microservicio de datos
 * Maneja todos los endpoints relacionados con productos, categorías e inventario
 * 
 * @author Agustin Benavidez
 */
@RestController
@RequestMapping("/data")
@CrossOrigin(origins = "*")
public class DataController {

    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final InventarioService inventarioService;

    @Autowired
    public DataController(ProductoService productoService,
                         CategoriaService categoriaService,
                         InventarioService inventarioService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.inventarioService = inventarioService;
    }

    // ========== ENDPOINTS DE PRODUCTOS ==========

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> obtenerTodosLosProductos() {
        logger.info("GET /data/productos - Obteniendo todos los productos");
        List<Producto> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        logger.info("GET /data/productos/{} - Obteniendo producto por ID", id);
        Producto producto = productoService.buscarPorId(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto,
                                                 @RequestParam(required = false) Integer cantidadInicial,
                                                 @RequestParam(required = false) Integer stockMinimo) {
        logger.info("POST /data/productos - Creando producto: {}", producto.getNombre());
        Producto productoCreado = productoService.crear(producto, cantidadInicial, stockMinimo);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id,
                                                      @Valid @RequestBody Producto producto) {
        logger.info("PUT /data/productos/{} - Actualizando producto", id);
        Producto productoActualizado = productoService.actualizar(id, producto);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        logger.info("DELETE /data/productos/{} - Eliminando producto", id);
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/productos/categoria/{nombre}")
    public ResponseEntity<List<Producto>> obtenerProductosPorCategoria(@PathVariable String nombre) {
        logger.info("GET /data/productos/categoria/{} - Obteniendo productos por categoría", nombre);
        List<Producto> productos = productoService.buscarPorCategoria(nombre);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/buscar")
    public ResponseEntity<List<Producto>> buscarProductos(@RequestParam String texto) {
        logger.info("GET /data/productos/buscar?texto={} - Buscando productos", texto);
        List<Producto> productos = productoService.buscarPorTexto(texto);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/precio")
    public ResponseEntity<List<Producto>> buscarProductosPorPrecio(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        logger.info("GET /data/productos/precio?min={}&max={} - Buscando productos por rango de precio", min, max);
        List<Producto> productos = productoService.buscarPorRangoPrecios(min, max);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/stock-bajo")
    public ResponseEntity<List<Producto>> obtenerProductosConStockBajo() {
        logger.info("GET /data/productos/stock-bajo - Obteniendo productos con stock bajo");
        List<Producto> productos = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/sin-stock")
    public ResponseEntity<List<Producto>> obtenerProductosSinStock() {
        logger.info("GET /data/productos/sin-stock - Obteniendo productos sin stock");
        List<Producto> productos = productoService.obtenerProductosSinStock();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/valor-inventario")
    public ResponseEntity<List<Map<String, Object>>> obtenerValorInventarioPorProducto() {
        logger.info("GET /data/productos/valor-inventario - Obteniendo valor de inventario por producto");
        List<Map<String, Object>> valores = productoService.obtenerValorInventarioPorProducto();
        return ResponseEntity.ok(valores);
    }

    @PutMapping("/productos/{id}/reactivar")
    public ResponseEntity<Producto> reactivarProducto(@PathVariable Long id) {
        logger.info("PUT /data/productos/{}/reactivar - Reactivando producto", id);
        Producto producto = productoService.reactivar(id);
        return ResponseEntity.ok(producto);
    }

    // ========== ENDPOINTS DE CATEGORÍAS ==========

    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> obtenerTodasLasCategorias() {
        logger.info("GET /data/categorias - Obteniendo todas las categorías");
        List<Categoria> categorias = categoriaService.obtenerTodas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/categorias/{id}")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Long id) {
        logger.info("GET /data/categorias/{} - Obteniendo categoría por ID", id);
        Categoria categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @GetMapping("/categorias/nombre/{nombre}")
    public ResponseEntity<Categoria> obtenerCategoriaPorNombre(@PathVariable String nombre) {
        logger.info("GET /data/categorias/nombre/{} - Obteniendo categoría por nombre", nombre);
        Categoria categoria = categoriaService.buscarPorNombre(nombre);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping("/categorias")
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody Categoria categoria) {
        logger.info("POST /data/categorias - Creando categoría: {}", categoria.getNombre());
        Categoria categoriaCreada = categoriaService.crear(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id,
                                                        @Valid @RequestBody Categoria categoria) {
        logger.info("PUT /data/categorias/{} - Actualizando categoría", id);
        Categoria categoriaActualizada = categoriaService.actualizar(id, categoria);
        return ResponseEntity.ok(categoriaActualizada);
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        logger.info("DELETE /data/categorias/{} - Eliminando categoría", id);
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorias/buscar")
    public ResponseEntity<List<Categoria>> buscarCategorias(@RequestParam String texto) {
        logger.info("GET /data/categorias/buscar?texto={} - Buscando categorías", texto);
        List<Categoria> categorias = categoriaService.buscarPorTexto(texto);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/categorias/con-productos")
    public ResponseEntity<List<Categoria>> obtenerCategoriasConProductos() {
        logger.info("GET /data/categorias/con-productos - Obteniendo categorías con productos");
        List<Categoria> categorias = categoriaService.obtenerCategoriasConProductos();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/categorias/estadisticas")
    public ResponseEntity<List<Map<String, Object>>> obtenerEstadisticasCategorias() {
        logger.info("GET /data/categorias/estadisticas - Obteniendo estadísticas de categorías");
        List<Map<String, Object>> estadisticas = categoriaService.obtenerEstadisticasDetalladas();
        return ResponseEntity.ok(estadisticas);
    }

    // ========== ENDPOINTS DE INVENTARIO ==========

    @GetMapping("/inventario")
    public ResponseEntity<List<Inventario>> obtenerTodoElInventario() {
        logger.info("GET /data/inventario - Obteniendo todo el inventario");
        List<Inventario> inventarios = inventarioService.obtenerTodos();
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/inventario/{id}")
    public ResponseEntity<Inventario> obtenerInventarioPorId(@PathVariable Long id) {
        logger.info("GET /data/inventario/{} - Obteniendo inventario por ID", id);
        Inventario inventario = inventarioService.buscarPorId(id);
        return ResponseEntity.ok(inventario);
    }

    @GetMapping("/inventario/producto/{productoId}")
    public ResponseEntity<Inventario> obtenerInventarioPorProducto(@PathVariable Long productoId) {
        logger.info("GET /data/inventario/producto/{} - Obteniendo inventario por producto", productoId);
        Inventario inventario = inventarioService.buscarPorProductoId(productoId);
        return ResponseEntity.ok(inventario);
    }

    @PostMapping("/inventario")
    public ResponseEntity<Inventario> crearInventario(@Valid @RequestBody Inventario inventario) {
        logger.info("POST /data/inventario - Creando inventario para producto ID: {}", 
                   inventario.getProducto() != null ? inventario.getProducto().getId() : "null");
        Inventario inventarioCreado = inventarioService.crear(inventario);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioCreado);
    }

    @PutMapping("/inventario/{id}")
    public ResponseEntity<Inventario> actualizarInventario(@PathVariable Long id,
                                                          @Valid @RequestBody Inventario inventario) {
        logger.info("PUT /data/inventario/{} - Actualizando inventario", id);
        Inventario inventarioActualizado = inventarioService.actualizar(id, inventario);
        return ResponseEntity.ok(inventarioActualizado);
    }

    @PutMapping("/inventario/producto/{productoId}/stock")
    public ResponseEntity<Inventario> actualizarStock(@PathVariable Long productoId,
                                                     @RequestParam Integer cantidad) {
        logger.info("PUT /data/inventario/producto/{}/stock - Actualizando stock a: {}", productoId, cantidad);
        Inventario inventario = inventarioService.actualizarStock(productoId, cantidad);
        return ResponseEntity.ok(inventario);
    }

    @PutMapping("/inventario/producto/{productoId}/incrementar")
    public ResponseEntity<Inventario> incrementarStock(@PathVariable Long productoId,
                                                      @RequestParam Integer incremento) {
        logger.info("PUT /data/inventario/producto/{}/incrementar - Incrementando stock en: {}", productoId, incremento);
        Inventario inventario = inventarioService.incrementarStock(productoId, incremento);
        return ResponseEntity.ok(inventario);
    }

    @PutMapping("/inventario/producto/{productoId}/decrementar")
    public ResponseEntity<Inventario> decrementarStock(@PathVariable Long productoId,
                                                      @RequestParam Integer decremento) {
        logger.info("PUT /data/inventario/producto/{}/decrementar - Decrementando stock en: {}", productoId, decremento);
        Inventario inventario = inventarioService.decrementarStock(productoId, decremento);
        return ResponseEntity.ok(inventario);
    }

    @GetMapping("/inventario/stock-bajo")
    public ResponseEntity<List<Inventario>> obtenerInventariosConStockBajo() {
        logger.info("GET /data/inventario/stock-bajo - Obteniendo inventarios con stock bajo");
        List<Inventario> inventarios = inventarioService.obtenerInventariosConStockBajo();
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/inventario/stock-critico")
    public ResponseEntity<List<Inventario>> obtenerInventariosConStockCritico() {
        logger.info("GET /data/inventario/stock-critico - Obteniendo inventarios con stock crítico");
        List<Inventario> inventarios = inventarioService.obtenerInventariosConStockCritico();
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/inventario/sin-stock")
    public ResponseEntity<List<Inventario>> obtenerInventariosSinStock() {
        logger.info("GET /data/inventario/sin-stock - Obteniendo inventarios sin stock");
        List<Inventario> inventarios = inventarioService.obtenerInventariosSinStock();
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/inventario/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasInventario() {
        logger.info("GET /data/inventario/estadisticas - Obteniendo estadísticas del inventario");
        Map<String, Object> estadisticas = inventarioService.obtenerEstadisticasInventario();
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/inventario/valor-total")
    public ResponseEntity<BigDecimal> obtenerValorTotalInventario() {
        logger.info("GET /data/inventario/valor-total - Calculando valor total del inventario");
        BigDecimal valorTotal = inventarioService.obtenerValorTotalInventario();
        return ResponseEntity.ok(valorTotal);
    }

    @GetMapping("/inventario/categoria/{categoriaNombre}")
    public ResponseEntity<List<Inventario>> obtenerInventarioPorCategoria(@PathVariable String categoriaNombre) {
        logger.info("GET /data/inventario/categoria/{} - Obteniendo inventario por categoría", categoriaNombre);
        List<Inventario> inventarios = inventarioService.buscarPorCategoria(categoriaNombre);
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/inventario/reabastecimiento")
    public ResponseEntity<List<Inventario>> obtenerInventariosParaReabastecimiento() {
        logger.info("GET /data/inventario/reabastecimiento - Obteniendo inventarios para reabastecimiento");
        List<Inventario> inventarios = inventarioService.obtenerInventariosParaReabastecimiento();
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/inventario/actualizados")
    public ResponseEntity<List<Inventario>> obtenerInventariosActualizadosRecientes(@RequestParam int dias) {
        logger.info("GET /data/inventario/actualizados?dias={} - Obteniendo inventarios actualizados", dias);
        LocalDateTime fecha = LocalDateTime.now().minusDays(dias);
        List<Inventario> inventarios = inventarioService.obtenerInventariosActualizadosDesde(fecha);
        return ResponseEntity.ok(inventarios);
    }

    // ========== ENDPOINT DE HEALTH CHECK ==========

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "data-service",
            "timestamp", LocalDateTime.now(),
            "version", "1.0.0"
        );
        return ResponseEntity.ok(health);
    }
}
