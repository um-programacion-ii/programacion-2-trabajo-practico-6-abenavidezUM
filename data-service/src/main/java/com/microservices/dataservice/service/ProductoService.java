package com.microservices.dataservice.service;

import com.microservices.dataservice.entity.Categoria;
import com.microservices.dataservice.entity.Inventario;
import com.microservices.dataservice.entity.Producto;
import com.microservices.dataservice.exception.DuplicateResourceException;
import com.microservices.dataservice.exception.ResourceNotFoundException;
import com.microservices.dataservice.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de productos
 * 
 * @author Agustin Benavidez
 */
@Service
@Transactional
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final InventarioService inventarioService;

    @Autowired
    public ProductoService(ProductoRepository productoRepository,
                          CategoriaService categoriaService,
                          InventarioService inventarioService) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.inventarioService = inventarioService;
    }

    /**
     * Obtiene todos los productos activos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        logger.debug("Obteniendo todos los productos activos");
        return productoRepository.findByActivoTrue();
    }

    /**
     * Busca un producto por ID
     */
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        logger.debug("Buscando producto con ID: {}", id);
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
    }

    /**
     * Busca productos por categoría
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String categoriaNombre) {
        logger.debug("Buscando productos de la categoría: {}", categoriaNombre);
        return productoRepository.findByCategoriaNombreIgnoreCase(categoriaNombre);
    }

    /**
     * Busca productos por categoría ID
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoriaId(Long categoriaId) {
        logger.debug("Buscando productos de la categoría ID: {}", categoriaId);
        return productoRepository.findByCategoriaId(categoriaId);
    }

    /**
     * Crea un nuevo producto con inventario inicial
     */
    public Producto crear(Producto producto, Integer cantidadInicial, Integer stockMinimo) {
        logger.info("Creando nuevo producto: {}", producto.getNombre());
        
        // Validar que no exista un producto con el mismo nombre
        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(producto.getNombre(), null)) {
            throw new DuplicateResourceException("Producto", "nombre", producto.getNombre());
        }

        // Validar que la categoría exista
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        Categoria categoria = categoriaService.buscarPorId(producto.getCategoria().getId());
        producto.setCategoria(categoria);

        // Guardar producto
        Producto productoGuardado = productoRepository.save(producto);
        logger.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());

        // Crear inventario inicial si se especificó
        if (cantidadInicial != null) {
            Inventario inventario = new Inventario(productoGuardado, cantidadInicial, stockMinimo != null ? stockMinimo : 0);
            inventarioService.crear(inventario);
            logger.info("Inventario inicial creado para producto ID: {} con cantidad: {}", 
                       productoGuardado.getId(), cantidadInicial);
        }

        return productoGuardado;
    }

    /**
     * Actualiza un producto existente
     */
    public Producto actualizar(Long id, Producto productoActualizado) {
        logger.info("Actualizando producto con ID: {}", id);
        
        Producto productoExistente = buscarPorId(id);

        // Validar que no exista otro producto con el mismo nombre
        if (!productoExistente.getNombre().equalsIgnoreCase(productoActualizado.getNombre()) &&
            productoRepository.existsByNombreIgnoreCaseAndIdNot(productoActualizado.getNombre(), id)) {
            throw new DuplicateResourceException("Producto", "nombre", productoActualizado.getNombre());
        }

        // Actualizar campos
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setPrecio(productoActualizado.getPrecio());

        // Actualizar categoría si cambió
        if (productoActualizado.getCategoria() != null && 
            productoActualizado.getCategoria().getId() != null &&
            !productoActualizado.getCategoria().getId().equals(productoExistente.getCategoria().getId())) {
            
            Categoria nuevaCategoria = categoriaService.buscarPorId(productoActualizado.getCategoria().getId());
            productoExistente.setCategoria(nuevaCategoria);
        }

        Producto productoGuardado = productoRepository.save(productoExistente);
        logger.info("Producto actualizado exitosamente: {}", productoGuardado.getNombre());
        return productoGuardado;
    }

    /**
     * Elimina un producto (soft delete)
     */
    public void eliminar(Long id) {
        logger.info("Eliminando producto con ID: {}", id);
        
        Producto producto = buscarPorId(id);
        producto.setActivo(false);
        
        productoRepository.save(producto);
        logger.info("Producto desactivado exitosamente: {}", producto.getNombre());
    }

    /**
     * Elimina permanentemente un producto
     */
    public void eliminarPermanentemente(Long id) {
        logger.warn("Eliminando permanentemente producto con ID: {}", id);
        
        Producto producto = buscarPorId(id);
        productoRepository.delete(producto);
        logger.warn("Producto eliminado permanentemente: {}", producto.getNombre());
    }

    /**
     * Busca productos por texto en nombre o descripción
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorTexto(String texto) {
        logger.debug("Buscando productos que contengan: {}", texto);
        return productoRepository.buscarPorTexto(texto);
    }

    /**
     * Busca productos por rango de precios
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorRangoPrecios(BigDecimal precioMin, BigDecimal precioMax) {
        logger.debug("Buscando productos con precio entre {} y {}", precioMin, precioMax);
        return productoRepository.findByPrecioBetween(precioMin, precioMax);
    }

    /**
     * Obtiene productos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockBajo() {
        logger.debug("Obteniendo productos con stock bajo");
        return productoRepository.findProductosConStockBajo();
    }

    /**
     * Obtiene productos sin stock
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosSinStock() {
        logger.debug("Obteniendo productos sin stock");
        return productoRepository.findProductosSinStock();
    }

    /**
     * Obtiene productos con stock crítico
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockCritico() {
        logger.debug("Obteniendo productos con stock crítico");
        return productoRepository.findProductosConStockCritico();
    }

    /**
     * Obtiene productos ordenados por precio (mayor a menor)
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorPrecioDesc() {
        logger.debug("Obteniendo productos ordenados por precio descendente");
        return productoRepository.findProductosOrdenadosPorPrecioDesc();
    }

    /**
     * Obtiene productos ordenados por precio (menor a mayor)
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorPrecioAsc() {
        logger.debug("Obteniendo productos ordenados por precio ascendente");
        return productoRepository.findProductosOrdenadosPorPrecioAsc();
    }

    /**
     * Obtiene el valor total del inventario por producto
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerValorInventarioPorProducto() {
        logger.debug("Obteniendo valor de inventario por producto");
        List<Object[]> resultados = productoRepository.obtenerValorInventarioPorProducto();
        
        return resultados.stream()
                .map(result -> Map.of(
                    "productoId", result[0],
                    "nombre", result[1],
                    "precio", result[2],
                    "cantidad", result[3],
                    "valorTotal", result[4]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Busca productos más caros que un precio dado
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorPrecioMayorA(BigDecimal precio) {
        logger.debug("Buscando productos con precio mayor a: {}", precio);
        return productoRepository.findByPrecioGreaterThan(precio);
    }

    /**
     * Busca productos más baratos que un precio dado
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorPrecioMenorA(BigDecimal precio) {
        logger.debug("Buscando productos con precio menor a: {}", precio);
        return productoRepository.findByPrecioLessThan(precio);
    }

    /**
     * Busca productos creados recientemente
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosRecientes(int dias) {
        logger.debug("Obteniendo productos creados en los últimos {} días", dias);
        return productoRepository.findProductosRecientes(dias);
    }

    /**
     * Cuenta productos por categoría
     */
    @Transactional(readOnly = true)
    public long contarPorCategoria(Long categoriaId) {
        return productoRepository.countByCategoriaId(categoriaId);
    }

    /**
     * Obtiene el total de productos activos
     */
    @Transactional(readOnly = true)
    public long contarProductosActivos() {
        return productoRepository.findByActivoTrue().size();
    }

    /**
     * Reactiva un producto
     */
    public Producto reactivar(Long id) {
        logger.info("Reactivando producto con ID: {}", id);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        
        producto.setActivo(true);
        Producto productoReactivado = productoRepository.save(producto);
        
        logger.info("Producto reactivado exitosamente: {}", productoReactivado.getNombre());
        return productoReactivado;
    }
}
