package com.microservices.dataservice.service;

import com.microservices.dataservice.entity.Inventario;
import com.microservices.dataservice.entity.Producto;
import com.microservices.dataservice.exception.InsufficientStockException;
import com.microservices.dataservice.exception.ResourceNotFoundException;
import com.microservices.dataservice.repository.InventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de inventario
 * 
 * @author Agustin Benavidez
 */
@Service
@Transactional
public class InventarioService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository inventarioRepository;

    @Autowired
    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    /**
     * Obtiene todos los inventarios
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerTodos() {
        logger.debug("Obteniendo todos los inventarios");
        return inventarioRepository.findAll();
    }

    /**
     * Busca un inventario por ID
     */
    @Transactional(readOnly = true)
    public Inventario buscarPorId(Long id) {
        logger.debug("Buscando inventario con ID: {}", id);
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", "id", id));
    }

    /**
     * Busca inventario por producto ID
     */
    @Transactional(readOnly = true)
    public Inventario buscarPorProductoId(Long productoId) {
        logger.debug("Buscando inventario para producto ID: {}", productoId);
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", "productoId", productoId));
    }

    /**
     * Crea un nuevo inventario
     */
    public Inventario crear(Inventario inventario) {
        logger.info("Creando nuevo inventario para producto ID: {}", 
                   inventario.getProducto() != null ? inventario.getProducto().getId() : "null");
        
        if (inventario.getProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio para crear un inventario");
        }

        // Verificar que no exista ya un inventario para este producto
        if (inventarioRepository.findByProductoId(inventario.getProducto().getId()).isPresent()) {
            throw new IllegalStateException("Ya existe un inventario para el producto ID: " + 
                                          inventario.getProducto().getId());
        }

        Inventario inventarioGuardado = inventarioRepository.save(inventario);
        logger.info("Inventario creado exitosamente con ID: {}", inventarioGuardado.getId());
        return inventarioGuardado;
    }

    /**
     * Actualiza un inventario existente
     */
    public Inventario actualizar(Long id, Inventario inventarioActualizado) {
        logger.info("Actualizando inventario con ID: {}", id);
        
        Inventario inventarioExistente = buscarPorId(id);

        // Actualizar campos
        inventarioExistente.setCantidad(inventarioActualizado.getCantidad());
        inventarioExistente.setStockMinimo(inventarioActualizado.getStockMinimo());

        Inventario inventarioGuardado = inventarioRepository.save(inventarioExistente);
        logger.info("Inventario actualizado exitosamente para producto: {}", 
                   inventarioGuardado.getProductoNombre());
        return inventarioGuardado;
    }

    /**
     * Actualiza la cantidad de stock de un producto
     */
    public Inventario actualizarStock(Long productoId, Integer nuevaCantidad) {
        logger.info("Actualizando stock del producto ID: {} a cantidad: {}", productoId, nuevaCantidad);
        
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        Inventario inventario = buscarPorProductoId(productoId);
        inventario.setCantidad(nuevaCantidad);
        
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        logger.info("Stock actualizado exitosamente para producto: {} - Nueva cantidad: {}", 
                   inventarioActualizado.getProductoNombre(), nuevaCantidad);
        return inventarioActualizado;
    }

    /**
     * Incrementa el stock de un producto
     */
    public Inventario incrementarStock(Long productoId, Integer incremento) {
        logger.info("Incrementando stock del producto ID: {} en: {}", productoId, incremento);
        
        if (incremento <= 0) {
            throw new IllegalArgumentException("El incremento debe ser mayor a cero");
        }

        Inventario inventario = buscarPorProductoId(productoId);
        inventario.incrementarStock(incremento);
        
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        logger.info("Stock incrementado exitosamente para producto: {} - Nueva cantidad: {}", 
                   inventarioActualizado.getProductoNombre(), inventarioActualizado.getCantidad());
        return inventarioActualizado;
    }

    /**
     * Decrementa el stock de un producto
     */
    public Inventario decrementarStock(Long productoId, Integer decremento) {
        logger.info("Decrementando stock del producto ID: {} en: {}", productoId, decremento);
        
        if (decremento <= 0) {
            throw new IllegalArgumentException("El decremento debe ser mayor a cero");
        }

        Inventario inventario = buscarPorProductoId(productoId);
        
        if (inventario.getCantidad() < decremento) {
            throw new InsufficientStockException(productoId, inventario.getCantidad(), decremento);
        }

        inventario.decrementarStock(decremento);
        
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        logger.info("Stock decrementado exitosamente para producto: {} - Nueva cantidad: {}", 
                   inventarioActualizado.getProductoNombre(), inventarioActualizado.getCantidad());
        
        // Log de advertencia si el stock queda bajo
        if (inventarioActualizado.isStockBajo()) {
            logger.warn("ALERTA: El producto '{}' tiene stock bajo. Cantidad actual: {}, Stock mínimo: {}", 
                       inventarioActualizado.getProductoNombre(), 
                       inventarioActualizado.getCantidad(), 
                       inventarioActualizado.getStockMinimo());
        }
        
        return inventarioActualizado;
    }

    /**
     * Obtiene inventarios con stock bajo
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventariosConStockBajo() {
        logger.debug("Obteniendo inventarios con stock bajo");
        return inventarioRepository.findInventariosConStockBajo();
    }

    /**
     * Obtiene inventarios con stock crítico
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventariosConStockCritico() {
        logger.debug("Obteniendo inventarios con stock crítico");
        return inventarioRepository.findInventariosConStockCritico();
    }

    /**
     * Obtiene inventarios sin stock
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventariosSinStock() {
        logger.debug("Obteniendo inventarios sin stock");
        return inventarioRepository.findInventariosSinStock();
    }

    /**
     * Busca inventarios por rango de cantidad
     */
    @Transactional(readOnly = true)
    public List<Inventario> buscarPorRangoCantidad(Integer cantidadMin, Integer cantidadMax) {
        logger.debug("Buscando inventarios con cantidad entre {} y {}", cantidadMin, cantidadMax);
        return inventarioRepository.findByCantidadBetween(cantidadMin, cantidadMax);
    }

    /**
     * Busca inventarios por categoría
     */
    @Transactional(readOnly = true)
    public List<Inventario> buscarPorCategoria(String categoriaNombre) {
        logger.debug("Buscando inventarios de la categoría: {}", categoriaNombre);
        return inventarioRepository.findByCategoria(categoriaNombre);
    }

    /**
     * Obtiene estadísticas generales del inventario
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasInventario() {
        logger.debug("Obteniendo estadísticas generales del inventario");
        Object[] resultado = inventarioRepository.obtenerEstadisticasInventario();
        
        return Map.of(
            "totalProductos", resultado[0],
            "cantidadTotalItems", resultado[1],
            "cantidadPromedio", resultado[2],
            "productosConStockBajo", resultado[3]
        );
    }

    /**
     * Obtiene el valor total del inventario
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerValorTotalInventario() {
        logger.debug("Calculando valor total del inventario");
        Object resultado = inventarioRepository.obtenerValorTotalInventario();
        return resultado != null ? (BigDecimal) resultado : BigDecimal.ZERO;
    }

    /**
     * Obtiene inventarios actualizados recientemente
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventariosActualizadosDesde(LocalDateTime fecha) {
        logger.debug("Obteniendo inventarios actualizados desde: {}", fecha);
        return inventarioRepository.findInventariosActualizadosDesde(fecha);
    }

    /**
     * Cuenta inventarios con stock bajo por categoría
     */
    @Transactional(readOnly = true)
    public Map<String, Long> contarStockBajoPorCategoria() {
        logger.debug("Contando inventarios con stock bajo por categoría");
        List<Object[]> resultados = inventarioRepository.contarStockBajoPorCategoria();
        
        return resultados.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],      // nombre de categoría
                    result -> ((Number) result[1]).longValue(),  // cantidad con stock bajo
                    (existing, replacement) -> existing
                ));
    }

    /**
     * Obtiene inventarios que necesitan reabastecimiento urgente
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventariosParaReabastecimiento() {
        logger.debug("Obteniendo inventarios que necesitan reabastecimiento urgente");
        return inventarioRepository.findInventariosParaReabastecimiento();
    }

    /**
     * Verifica si un producto tiene suficiente stock
     */
    @Transactional(readOnly = true)
    public boolean verificarSuficienteStock(Long productoId, Integer cantidadRequerida) {
        logger.debug("Verificando si producto ID: {} tiene stock suficiente para cantidad: {}", 
                    productoId, cantidadRequerida);
        return inventarioRepository.tieneSuficienteStock(productoId, cantidadRequerida);
    }

    /**
     * Actualiza el stock mínimo de un producto
     */
    public Inventario actualizarStockMinimo(Long productoId, Integer nuevoStockMinimo) {
        logger.info("Actualizando stock mínimo del producto ID: {} a: {}", productoId, nuevoStockMinimo);
        
        if (nuevoStockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }

        Inventario inventario = buscarPorProductoId(productoId);
        inventario.setStockMinimo(nuevoStockMinimo);
        
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        logger.info("Stock mínimo actualizado exitosamente para producto: {} - Nuevo stock mínimo: {}", 
                   inventarioActualizado.getProductoNombre(), nuevoStockMinimo);
        return inventarioActualizado;
    }

    /**
     * Elimina un inventario
     */
    public void eliminar(Long id) {
        logger.info("Eliminando inventario con ID: {}", id);
        
        Inventario inventario = buscarPorId(id);
        inventarioRepository.delete(inventario);
        
        logger.info("Inventario eliminado exitosamente para producto: {}", 
                   inventario.getProductoNombre());
    }
}
