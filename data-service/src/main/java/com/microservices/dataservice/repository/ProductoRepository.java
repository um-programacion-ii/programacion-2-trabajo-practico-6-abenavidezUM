package com.microservices.dataservice.repository;

import com.microservices.dataservice.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Producto
 * 
 * @author Agustin Benavidez
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por nombre de categoría
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.nombre = :categoriaNombre AND p.activo = true")
    List<Producto> findByCategoriaNombre(@Param("categoriaNombre") String categoriaNombre);

    /**
     * Busca productos por nombre de categoría (case insensitive)
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.categoria.nombre) = LOWER(:categoriaNombre) AND p.activo = true")
    List<Producto> findByCategoriaNombreIgnoreCase(@Param("categoriaNombre") String categoriaNombre);

    /**
     * Busca productos activos
     */
    List<Producto> findByActivoTrue();

    /**
     * Busca productos por rango de precios
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax AND p.activo = true ORDER BY p.precio")
    List<Producto> findByPrecioBetween(@Param("precioMin") BigDecimal precioMin, @Param("precioMax") BigDecimal precioMax);

    /**
     * Busca productos que contengan el texto en nombre o descripción
     */
    @Query("SELECT p FROM Producto p WHERE " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "AND p.activo = true")
    List<Producto> buscarPorTexto(@Param("texto") String texto);

    /**
     * Busca productos con stock bajo
     */
    @Query("SELECT p FROM Producto p " +
           "INNER JOIN p.inventario i " +
           "WHERE i.cantidad <= i.stockMinimo AND p.activo = true " +
           "ORDER BY i.cantidad ASC")
    List<Producto> findProductosConStockBajo();

    /**
     * Busca productos sin stock
     */
    @Query("SELECT p FROM Producto p " +
           "INNER JOIN p.inventario i " +
           "WHERE i.cantidad = 0 AND p.activo = true")
    List<Producto> findProductosSinStock();

    /**
     * Busca productos más caros
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.precio DESC")
    List<Producto> findProductosOrdenadosPorPrecioDesc();

    /**
     * Busca productos más baratos
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.precio ASC")
    List<Producto> findProductosOrdenadosPorPrecioAsc();

    /**
     * Busca productos por categoría ID
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId AND p.activo = true")
    List<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    /**
     * Cuenta productos por categoría
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.activo = true")
    long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    /**
     * Obtiene el valor total del inventario por producto
     */
    @Query("SELECT p.id, p.nombre, p.precio, i.cantidad, (p.precio * i.cantidad) as valorTotal " +
           "FROM Producto p " +
           "INNER JOIN p.inventario i " +
           "WHERE p.activo = true " +
           "ORDER BY valorTotal DESC")
    List<Object[]> obtenerValorInventarioPorProducto();

    /**
     * Busca productos con inventario crítico (menos del 50% del stock mínimo)
     */
    @Query("SELECT p FROM Producto p " +
           "INNER JOIN p.inventario i " +
           "WHERE i.cantidad <= (i.stockMinimo * 0.5) AND p.activo = true " +
           "ORDER BY i.cantidad ASC")
    List<Producto> findProductosConStockCritico();

    /**
     * Busca productos por precio mayor a
     */
    @Query("SELECT p FROM Producto p WHERE p.precio > :precio AND p.activo = true ORDER BY p.precio ASC")
    List<Producto> findByPrecioGreaterThan(@Param("precio") BigDecimal precio);

    /**
     * Busca productos por precio menor a
     */
    @Query("SELECT p FROM Producto p WHERE p.precio < :precio AND p.activo = true ORDER BY p.precio DESC")
    List<Producto> findByPrecioLessThan(@Param("precio") BigDecimal precio);

    /**
     * Verifica si existe un producto con el nombre dado (excluyendo el ID actual)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p " +
           "WHERE LOWER(p.nombre) = LOWER(:nombre) AND (:id IS NULL OR p.id != :id)")
    boolean existsByNombreIgnoreCaseAndIdNot(@Param("nombre") String nombre, @Param("id") Long id);

    /**
     * Busca productos creados recientemente (últimos N días)
     */
    @Query("SELECT p FROM Producto p " +
           "WHERE p.fechaCreacion >= CURRENT_TIMESTAMP - :dias DAY " +
           "AND p.activo = true " +
           "ORDER BY p.fechaCreacion DESC")
    List<Producto> findProductosRecientes(@Param("dias") int dias);
}
