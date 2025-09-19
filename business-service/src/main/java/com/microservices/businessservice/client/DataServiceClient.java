package com.microservices.businessservice.client;

import com.microservices.businessservice.dto.CategoriaDTO;
import com.microservices.businessservice.dto.InventarioDTO;
import com.microservices.businessservice.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Cliente Feign para comunicación con el microservicio data-service
 * 
 * @author Agustin Benavidez
 */
@FeignClient(
    name = "data-service",
    url = "${data.service.url:http://localhost:8081}",
    fallback = DataServiceClientFallback.class
)
public interface DataServiceClient {

    // ========== ENDPOINTS DE PRODUCTOS ==========

    @GetMapping("/data/productos")
    List<ProductoDTO> obtenerTodosLosProductos();

    @GetMapping("/data/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);

    @PostMapping("/data/productos")
    ProductoDTO crearProducto(@RequestBody ProductoDTO producto,
                             @RequestParam(required = false) Integer cantidadInicial,
                             @RequestParam(required = false) Integer stockMinimo);

    @PutMapping("/data/productos/{id}")
    ProductoDTO actualizarProducto(@PathVariable("id") Long id, @RequestBody ProductoDTO producto);

    @DeleteMapping("/data/productos/{id}")
    void eliminarProducto(@PathVariable("id") Long id);

    @GetMapping("/data/productos/categoria/{nombre}")
    List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable("nombre") String categoriaNombre);

    @GetMapping("/data/productos/buscar")
    List<ProductoDTO> buscarProductos(@RequestParam("texto") String texto);

    @GetMapping("/data/productos/precio")
    List<ProductoDTO> buscarProductosPorPrecio(@RequestParam("min") BigDecimal min,
                                              @RequestParam("max") BigDecimal max);

    @GetMapping("/data/productos/stock-bajo")
    List<ProductoDTO> obtenerProductosConStockBajo();

    @GetMapping("/data/productos/sin-stock")
    List<ProductoDTO> obtenerProductosSinStock();

    @GetMapping("/data/productos/valor-inventario")
    List<Map<String, Object>> obtenerValorInventarioPorProducto();

    @PutMapping("/data/productos/{id}/reactivar")
    ProductoDTO reactivarProducto(@PathVariable("id") Long id);

    // ========== ENDPOINTS DE CATEGORÍAS ==========

    @GetMapping("/data/categorias")
    List<CategoriaDTO> obtenerTodasLasCategorias();

    @GetMapping("/data/categorias/{id}")
    CategoriaDTO obtenerCategoriaPorId(@PathVariable("id") Long id);

    @GetMapping("/data/categorias/nombre/{nombre}")
    CategoriaDTO obtenerCategoriaPorNombre(@PathVariable("nombre") String nombre);

    @PostMapping("/data/categorias")
    CategoriaDTO crearCategoria(@RequestBody CategoriaDTO categoria);

    @PutMapping("/data/categorias/{id}")
    CategoriaDTO actualizarCategoria(@PathVariable("id") Long id, @RequestBody CategoriaDTO categoria);

    @DeleteMapping("/data/categorias/{id}")
    void eliminarCategoria(@PathVariable("id") Long id);

    @GetMapping("/data/categorias/buscar")
    List<CategoriaDTO> buscarCategorias(@RequestParam("texto") String texto);

    @GetMapping("/data/categorias/con-productos")
    List<CategoriaDTO> obtenerCategoriasConProductos();

    @GetMapping("/data/categorias/estadisticas")
    List<Map<String, Object>> obtenerEstadisticasCategorias();

    // ========== ENDPOINTS DE INVENTARIO ==========

    @GetMapping("/data/inventario")
    List<InventarioDTO> obtenerTodoElInventario();

    @GetMapping("/data/inventario/{id}")
    InventarioDTO obtenerInventarioPorId(@PathVariable("id") Long id);

    @GetMapping("/data/inventario/producto/{productoId}")
    InventarioDTO obtenerInventarioPorProducto(@PathVariable("productoId") Long productoId);

    @PostMapping("/data/inventario")
    InventarioDTO crearInventario(@RequestBody InventarioDTO inventario);

    @PutMapping("/data/inventario/{id}")
    InventarioDTO actualizarInventario(@PathVariable("id") Long id, @RequestBody InventarioDTO inventario);

    @PutMapping("/data/inventario/producto/{productoId}/stock")
    InventarioDTO actualizarStock(@PathVariable("productoId") Long productoId,
                                 @RequestParam("cantidad") Integer cantidad);

    @PutMapping("/data/inventario/producto/{productoId}/incrementar")
    InventarioDTO incrementarStock(@PathVariable("productoId") Long productoId,
                                  @RequestParam("incremento") Integer incremento);

    @PutMapping("/data/inventario/producto/{productoId}/decrementar")
    InventarioDTO decrementarStock(@PathVariable("productoId") Long productoId,
                                  @RequestParam("decremento") Integer decremento);

    @GetMapping("/data/inventario/stock-bajo")
    List<InventarioDTO> obtenerInventariosConStockBajo();

    @GetMapping("/data/inventario/stock-critico")
    List<InventarioDTO> obtenerInventariosConStockCritico();

    @GetMapping("/data/inventario/sin-stock")
    List<InventarioDTO> obtenerInventariosSinStock();

    @GetMapping("/data/inventario/estadisticas")
    Map<String, Object> obtenerEstadisticasInventario();

    @GetMapping("/data/inventario/valor-total")
    BigDecimal obtenerValorTotalInventario();

    @GetMapping("/data/inventario/categoria/{categoriaNombre}")
    List<InventarioDTO> obtenerInventarioPorCategoria(@PathVariable("categoriaNombre") String categoriaNombre);

    @GetMapping("/data/inventario/reabastecimiento")
    List<InventarioDTO> obtenerInventariosParaReabastecimiento();

    @GetMapping("/data/inventario/actualizados")
    List<InventarioDTO> obtenerInventariosActualizadosRecientes(@RequestParam("dias") int dias);

    // ========== HEALTH CHECK ==========

    @GetMapping("/data/health")
    Map<String, Object> healthCheck();
}
