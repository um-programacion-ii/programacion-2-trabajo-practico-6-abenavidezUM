package com.microservices.dataservice.repository;

import com.microservices.dataservice.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Categoria
 * 
 * @author Agustin Benavidez
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por nombre (case insensitive)
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe una categoría con el nombre dado
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca categorías que contengan el texto en el nombre o descripción
     */
    @Query("SELECT c FROM Categoria c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Categoria> buscarPorTexto(@Param("texto") String texto);

    /**
     * Obtiene categorías con productos activos
     */
    @Query("SELECT DISTINCT c FROM Categoria c " +
           "INNER JOIN c.productos p " +
           "WHERE p.activo = true")
    List<Categoria> findCategoriasConProductosActivos();

    /**
     * Obtiene categorías sin productos
     */
    @Query("SELECT c FROM Categoria c WHERE c.productos IS EMPTY")
    List<Categoria> findCategoriasSinProductos();

    /**
     * Cuenta productos por categoría
     */
    @Query("SELECT c.nombre, COUNT(p) FROM Categoria c " +
           "LEFT JOIN c.productos p " +
           "WHERE p.activo = true OR p IS NULL " +
           "GROUP BY c.id, c.nombre " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> contarProductosPorCategoria();

    /**
     * Obtiene estadísticas de categorías
     */
    @Query("SELECT c.id, c.nombre, COUNT(p), " +
           "COALESCE(SUM(p.precio * COALESCE(i.cantidad, 0)), 0) as valorTotal " +
           "FROM Categoria c " +
           "LEFT JOIN c.productos p ON p.activo = true " +
           "LEFT JOIN p.inventario i " +
           "GROUP BY c.id, c.nombre " +
           "ORDER BY c.nombre")
    List<Object[]> obtenerEstadisticasCategorias();

    /**
     * Busca categorías ordenadas por cantidad de productos
     */
    @Query("SELECT c FROM Categoria c " +
           "LEFT JOIN c.productos p " +
           "GROUP BY c " +
           "ORDER BY COUNT(p) DESC")
    List<Categoria> findCategoriasOrdenadaPorCantidadProductos();
}
