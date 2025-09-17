package com.microservices.dataservice.service;

import com.microservices.dataservice.entity.Categoria;
import com.microservices.dataservice.exception.DuplicateResourceException;
import com.microservices.dataservice.exception.ResourceNotFoundException;
import com.microservices.dataservice.repository.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de categorías
 * 
 * @author Agustin Benavidez
 */
@Service
@Transactional
public class CategoriaService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Obtiene todas las categorías
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodas() {
        logger.debug("Obteniendo todas las categorías");
        return categoriaRepository.findAll();
    }

    /**
     * Busca una categoría por ID
     */
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        logger.debug("Buscando categoría con ID: {}", id);
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));
    }

    /**
     * Busca una categoría por nombre
     */
    @Transactional(readOnly = true)
    public Categoria buscarPorNombre(String nombre) {
        logger.debug("Buscando categoría con nombre: {}", nombre);
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "nombre", nombre));
    }

    /**
     * Crea una nueva categoría
     */
    public Categoria crear(Categoria categoria) {
        logger.info("Creando nueva categoría: {}", categoria.getNombre());
        
        // Validar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new DuplicateResourceException("Categoria", "nombre", categoria.getNombre());
        }

        Categoria categoriaNueva = categoriaRepository.save(categoria);
        logger.info("Categoría creada exitosamente con ID: {}", categoriaNueva.getId());
        return categoriaNueva;
    }

    /**
     * Actualiza una categoría existente
     */
    public Categoria actualizar(Long id, Categoria categoriaActualizada) {
        logger.info("Actualizando categoría con ID: {}", id);
        
        Categoria categoriaExistente = buscarPorId(id);

        // Validar que no exista otra categoría con el mismo nombre
        if (!categoriaExistente.getNombre().equalsIgnoreCase(categoriaActualizada.getNombre()) &&
            categoriaRepository.existsByNombreIgnoreCase(categoriaActualizada.getNombre())) {
            throw new DuplicateResourceException("Categoria", "nombre", categoriaActualizada.getNombre());
        }

        // Actualizar campos
        categoriaExistente.setNombre(categoriaActualizada.getNombre());
        categoriaExistente.setDescripcion(categoriaActualizada.getDescripcion());

        Categoria categoriaGuardada = categoriaRepository.save(categoriaExistente);
        logger.info("Categoría actualizada exitosamente: {}", categoriaGuardada.getNombre());
        return categoriaGuardada;
    }

    /**
     * Elimina una categoría por ID
     */
    public void eliminar(Long id) {
        logger.info("Eliminando categoría con ID: {}", id);
        
        Categoria categoria = buscarPorId(id);
        
        // Verificar si la categoría tiene productos
        if (!categoria.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría '" + categoria.getNombre() + 
                                          "' porque tiene productos asociados");
        }

        categoriaRepository.delete(categoria);
        logger.info("Categoría eliminada exitosamente: {}", categoria.getNombre());
    }

    /**
     * Busca categorías por texto en nombre o descripción
     */
    @Transactional(readOnly = true)
    public List<Categoria> buscarPorTexto(String texto) {
        logger.debug("Buscando categorías que contengan: {}", texto);
        return categoriaRepository.buscarPorTexto(texto);
    }

    /**
     * Obtiene categorías que tienen productos activos
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasConProductos() {
        logger.debug("Obteniendo categorías con productos activos");
        return categoriaRepository.findCategoriasConProductosActivos();
    }

    /**
     * Obtiene categorías sin productos
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasSinProductos() {
        logger.debug("Obteniendo categorías sin productos");
        return categoriaRepository.findCategoriasSinProductos();
    }

    /**
     * Cuenta productos por categoría
     */
    @Transactional(readOnly = true)
    public Map<String, Long> contarProductosPorCategoria() {
        logger.debug("Contando productos por categoría");
        List<Object[]> resultados = categoriaRepository.contarProductosPorCategoria();
        
        return resultados.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],      // nombre de categoría
                    result -> ((Number) result[1]).longValue(),  // cantidad de productos
                    (existing, replacement) -> existing
                ));
    }

    /**
     * Obtiene estadísticas detalladas de categorías
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerEstadisticasDetalladas() {
        logger.debug("Obteniendo estadísticas detalladas de categorías");
        List<Object[]> resultados = categoriaRepository.obtenerEstadisticasCategorias();
        
        return resultados.stream()
                .map(result -> Map.of(
                    "id", result[0],
                    "nombre", result[1],
                    "cantidadProductos", result[2],
                    "valorTotalInventario", result[3]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene categorías ordenadas por cantidad de productos
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasOrdenadaPorProductos() {
        logger.debug("Obteniendo categorías ordenadas por cantidad de productos");
        return categoriaRepository.findCategoriasOrdenadaPorCantidadProductos();
    }

    /**
     * Verifica si existe una categoría con el nombre dado
     */
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return categoriaRepository.existsByNombreIgnoreCase(nombre);
    }

    /**
     * Obtiene el total de categorías
     */
    @Transactional(readOnly = true)
    public long contarTotal() {
        return categoriaRepository.count();
    }
}
