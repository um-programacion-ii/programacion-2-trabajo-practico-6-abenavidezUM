package com.microservices.dataservice.repository;

import com.microservices.dataservice.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Inventario
 * 
 * @author Agustin Benavidez
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Busca inventario por producto ID
     */
    Optional<Inventario> findByProductoId(Long productoId);

    /**
     * Busca inventarios con stock bajo
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.cantidad <= i.stockMinimo AND p.activo = true " +
           "ORDER BY i.cantidad ASC")
    List<Inventario> findInventariosConStockBajo();

    /**
     * Busca inventarios con stock crítico (menos del 50% del mínimo)
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.cantidad <= (i.stockMinimo * 0.5) AND p.activo = true " +
           "ORDER BY i.cantidad ASC")
    List<Inventario> findInventariosConStockCritico();

    /**
     * Busca inventarios sin stock
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.cantidad = 0 AND p.activo = true")
    List<Inventario> findInventariosSinStock();

    /**
     * Busca inventarios por rango de cantidad
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.cantidad BETWEEN :cantidadMin AND :cantidadMax AND p.activo = true " +
           "ORDER BY i.cantidad DESC")
    List<Inventario> findByCantidadBetween(@Param("cantidadMin") Integer cantidadMin, @Param("cantidadMax") Integer cantidadMax);

    /**
     * Busca inventarios por categoría
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE p.categoria.nombre = :categoriaNombre AND p.activo = true")
    List<Inventario> findByCategoria(@Param("categoriaNombre") String categoriaNombre);

    /**
     * Actualiza la cantidad de un inventario
     */
    @Modifying
    @Query("UPDATE Inventario i SET i.cantidad = :cantidad, i.fechaUltimaActualizacion = :fecha " +
           "WHERE i.producto.id = :productoId")
    int actualizarCantidadPorProductoId(@Param("productoId") Long productoId, 
                                       @Param("cantidad") Integer cantidad,
                                       @Param("fecha") LocalDateTime fecha);

    /**
     * Incrementa el stock de un producto
     */
    @Modifying
    @Query("UPDATE Inventario i SET i.cantidad = i.cantidad + :incremento, " +
           "i.fechaUltimaActualizacion = CURRENT_TIMESTAMP " +
           "WHERE i.producto.id = :productoId")
    int incrementarStock(@Param("productoId") Long productoId, @Param("incremento") Integer incremento);

    /**
     * Decrementa el stock de un producto (solo si hay suficiente stock)
     */
    @Modifying
    @Query("UPDATE Inventario i SET i.cantidad = i.cantidad - :decremento, " +
           "i.fechaUltimaActualizacion = CURRENT_TIMESTAMP " +
           "WHERE i.producto.id = :productoId AND i.cantidad >= :decremento")
    int decrementarStock(@Param("productoId") Long productoId, @Param("decremento") Integer decremento);

    /**
     * Obtiene estadísticas de inventario
     */
    @Query("SELECT COUNT(i), SUM(i.cantidad), AVG(i.cantidad), " +
           "SUM(CASE WHEN i.cantidad <= i.stockMinimo THEN 1 ELSE 0 END) as stockBajo " +
           "FROM Inventario i INNER JOIN i.producto p WHERE p.activo = true")
    Object[] obtenerEstadisticasInventario();

    /**
     * Obtiene el valor total del inventario
     */
    @Query("SELECT SUM(p.precio * i.cantidad) FROM Inventario i " +
           "INNER JOIN i.producto p WHERE p.activo = true")
    Object obtenerValorTotalInventario();

    /**
     * Busca inventarios actualizados recientemente
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.fechaUltimaActualizacion >= :fecha AND p.activo = true " +
           "ORDER BY i.fechaUltimaActualizacion DESC")
    List<Inventario> findInventariosActualizadosDesde(@Param("fecha") LocalDateTime fecha);

    /**
     * Cuenta inventarios con stock bajo por categoría
     */
    @Query("SELECT p.categoria.nombre, COUNT(i) FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.cantidad <= i.stockMinimo AND p.activo = true " +
           "GROUP BY p.categoria.nombre " +
           "ORDER BY COUNT(i) DESC")
    List<Object[]> contarStockBajoPorCategoria();

    /**
     * Busca inventarios que necesitan reabastecimiento urgente
     */
    @Query("SELECT i FROM Inventario i " +
           "INNER JOIN i.producto p " +
           "WHERE i.cantidad = 0 OR i.cantidad <= (i.stockMinimo * 0.2) " +
           "AND p.activo = true " +
           "ORDER BY i.cantidad ASC, p.categoria.nombre")
    List<Inventario> findInventariosParaReabastecimiento();

    /**
     * Verifica si un producto tiene suficiente stock
     */
    @Query("SELECT CASE WHEN i.cantidad >= :cantidadRequerida THEN true ELSE false END " +
           "FROM Inventario i WHERE i.producto.id = :productoId")
    boolean tieneSuficienteStock(@Param("productoId") Long productoId, @Param("cantidadRequerida") Integer cantidadRequerida);
}
