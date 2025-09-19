package com.microservices.businessservice.service;

import com.microservices.businessservice.client.DataServiceClient;
import com.microservices.businessservice.dto.*;
import com.microservices.businessservice.exception.ServiceUnavailableException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la generación de reportes
 * 
 * @author Agustin Benavidez
 */
@Service
public class ReporteBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteBusinessService.class);

    private final DataServiceClient dataServiceClient;

    @Autowired
    public ReporteBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    /**
     * Genera reporte completo del estado del inventario
     */
    public ReporteDTO generarReporteEstadoInventario() {
        logger.info("Generando reporte de estado de inventario");
        
        try {
            ReporteDTO reporte = new ReporteDTO("ESTADO_INVENTARIO", "Estado General del Inventario");
            reporte.setDescripcion("Reporte completo del estado actual del inventario");
            
            // Obtener datos básicos
            Map<String, Object> estadisticas = dataServiceClient.obtenerEstadisticasInventario();
            List<InventarioDTO> inventarios = dataServiceClient.obtenerTodoElInventario();
            List<InventarioDTO> stockBajo = dataServiceClient.obtenerInventariosConStockBajo();
            List<InventarioDTO> stockCritico = dataServiceClient.obtenerInventariosConStockCritico();
            List<InventarioDTO> sinStock = dataServiceClient.obtenerInventariosSinStock();
            
            // Calcular métricas
            reporte.setTotalProductos(inventarios.size());
            reporte.setProductosConStockBajo(stockBajo.size());
            reporte.setProductosConStockCritico(stockCritico.size());
            reporte.setProductosSinStock(sinStock.size());
            
            // Calcular valor total
            BigDecimal valorTotal = dataServiceClient.obtenerValorTotalInventario();
            reporte.setValorTotalInventario(valorTotal);
            
            // Calcular valor promedio
            if (!inventarios.isEmpty() && valorTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal valorPromedio = valorTotal.divide(BigDecimal.valueOf(inventarios.size()), 2, RoundingMode.HALF_UP);
                reporte.setValorPromedioProducto(valorPromedio);
            }
            
            // Agregar listas detalladas
            reporte.setInventarios(inventarios);
            
            // Datos adicionales
            Map<String, Object> datos = new HashMap<>();
            datos.put("estadisticasGenerales", estadisticas);
            datos.put("porcentajeStockBajo", calcularPorcentaje(stockBajo.size(), inventarios.size()));
            datos.put("porcentajeStockCritico", calcularPorcentaje(stockCritico.size(), inventarios.size()));
            datos.put("porcentajeSinStock", calcularPorcentaje(sinStock.size(), inventarios.size()));
            reporte.setDatos(datos);
            
            logger.info("Reporte de inventario generado: {} productos, valor total: {}", 
                       inventarios.size(), valorTotal);
            
            return reporte;
            
        } catch (FeignException e) {
            logger.error("Error al generar reporte de inventario: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "generarReporteEstadoInventario", e);
        }
    }

    /**
     * Genera reporte de productos por categoría
     */
    public ReporteDTO generarReporteProductosPorCategoria() {
        logger.info("Generando reporte de productos por categoría");
        
        try {
            ReporteDTO reporte = new ReporteDTO("PRODUCTOS_POR_CATEGORIA", "Productos por Categoría");
            reporte.setDescripcion("Distribución de productos por categoría con estadísticas");
            
            // Obtener datos
            List<CategoriaDTO> categorias = dataServiceClient.obtenerTodasLasCategorias();
            List<Map<String, Object>> estadisticasCategorias = dataServiceClient.obtenerEstadisticasCategorias();
            
            // Procesar estadísticas por categoría
            Map<String, Integer> productosPorCategoria = new HashMap<>();
            Map<String, BigDecimal> valorPorCategoria = new HashMap<>();
            
            for (Map<String, Object> stat : estadisticasCategorias) {
                String nombre = (String) stat.get("nombre");
                Integer cantidad = ((Number) stat.get("cantidadProductos")).intValue();
                BigDecimal valor = (BigDecimal) stat.get("valorTotalInventario");
                
                productosPorCategoria.put(nombre, cantidad);
                valorPorCategoria.put(nombre, valor != null ? valor : BigDecimal.ZERO);
            }
            
            reporte.setCategorias(categorias);
            reporte.setProductosPorCategoria(productosPorCategoria);
            reporte.setValorPorCategoria(valorPorCategoria);
            
            // Calcular totales
            int totalProductos = productosPorCategoria.values().stream().mapToInt(Integer::intValue).sum();
            BigDecimal valorTotal = valorPorCategoria.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            reporte.setTotalProductos(totalProductos);
            reporte.setValorTotalInventario(valorTotal);
            
            // Datos adicionales
            Map<String, Object> datos = new HashMap<>();
            datos.put("totalCategorias", categorias.size());
            datos.put("categoriaMasProductos", encontrarCategoriaMasProductos(productosPorCategoria));
            datos.put("categoriaMasValiosa", encontrarCategoriaMasValiosa(valorPorCategoria));
            reporte.setDatos(datos);
            
            logger.info("Reporte por categoría generado: {} categorías, {} productos total", 
                       categorias.size(), totalProductos);
            
            return reporte;
            
        } catch (FeignException e) {
            logger.error("Error al generar reporte por categoría: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "generarReporteProductosPorCategoria", e);
        }
    }

    /**
     * Genera reporte de alertas de stock
     */
    public ReporteDTO generarReporteAlertasStock() {
        logger.info("Generando reporte de alertas de stock");
        
        try {
            ReporteDTO reporte = new ReporteDTO("ALERTAS_STOCK", "Alertas de Stock");
            reporte.setDescripcion("Productos que requieren atención por niveles de stock");
            
            // Obtener datos de alertas
            List<InventarioDTO> stockBajo = dataServiceClient.obtenerInventariosConStockBajo();
            List<InventarioDTO> stockCritico = dataServiceClient.obtenerInventariosConStockCritico();
            List<InventarioDTO> sinStock = dataServiceClient.obtenerInventariosSinStock();
            List<InventarioDTO> reabastecimiento = dataServiceClient.obtenerInventariosParaReabastecimiento();
            
            // Clasificar por nivel de urgencia
            Map<String, List<InventarioDTO>> alertasPorNivel = new HashMap<>();
            alertasPorNivel.put("SIN_STOCK", sinStock);
            alertasPorNivel.put("CRITICO", stockCritico);
            alertasPorNivel.put("BAJO", stockBajo);
            alertasPorNivel.put("REABASTECIMIENTO", reabastecimiento);
            
            reporte.setInventarios(reabastecimiento);
            reporte.setProductosSinStock(sinStock.size());
            reporte.setProductosConStockCritico(stockCritico.size());
            reporte.setProductosConStockBajo(stockBajo.size());
            
            // Calcular impacto económico
            BigDecimal impactoSinStock = calcularImpactoEconomico(sinStock);
            BigDecimal impactoCritico = calcularImpactoEconomico(stockCritico);
            
            // Datos adicionales
            Map<String, Object> datos = new HashMap<>();
            datos.put("alertasPorNivel", alertasPorNivel);
            datos.put("impactoEconomicoSinStock", impactoSinStock);
            datos.put("impactoEconomicoCritico", impactoCritico);
            datos.put("totalProductosConAlertas", 
                     sinStock.size() + stockCritico.size() + stockBajo.size());
            reporte.setDatos(datos);
            
            // Generar recomendaciones
            generarRecomendacionesStock(reporte, alertasPorNivel);
            
            logger.warn("ALERTAS GENERADAS: {} sin stock, {} crítico, {} bajo", 
                       sinStock.size(), stockCritico.size(), stockBajo.size());
            
            return reporte;
            
        } catch (FeignException e) {
            logger.error("Error al generar reporte de alertas: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "generarReporteAlertasStock", e);
        }
    }

    /**
     * Genera reporte financiero
     */
    public ReporteDTO generarReporteFinanciero() {
        logger.info("Generando reporte financiero");
        
        try {
            ReporteDTO reporte = new ReporteDTO("FINANCIERO", "Reporte Financiero");
            reporte.setDescripcion("Análisis financiero del inventario y productos");
            
            // Obtener datos financieros
            BigDecimal valorTotal = dataServiceClient.obtenerValorTotalInventario();
            List<Map<String, Object>> valorPorProducto = dataServiceClient.obtenerValorInventarioPorProducto();
            List<CategoriaDTO> categorias = dataServiceClient.obtenerTodasLasCategorias();
            
            // Procesar datos financieros
            reporte.setValorTotalInventario(valorTotal);
            
            // Encontrar productos más valiosos
            List<Map<String, Object>> topProductos = valorPorProducto.stream()
                    .sorted((a, b) -> ((BigDecimal) b.get("valorTotal")).compareTo((BigDecimal) a.get("valorTotal")))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Calcular distribución de valor por categoría
            Map<String, BigDecimal> valorPorCategoria = calcularValorPorCategoria();
            reporte.setValorPorCategoria(valorPorCategoria);
            
            // Datos adicionales
            Map<String, Object> datos = new HashMap<>();
            datos.put("topProductosMasValiosos", topProductos);
            datos.put("distribucuionValor", valorPorCategoria);
            datos.put("valorPromedioPorProducto", 
                     valorTotal.divide(BigDecimal.valueOf(valorPorProducto.size()), 2, RoundingMode.HALF_UP));
            reporte.setDatos(datos);
            
            logger.info("Reporte financiero generado: valor total {}", valorTotal);
            
            return reporte;
            
        } catch (FeignException e) {
            logger.error("Error al generar reporte financiero: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "generarReporteFinanciero", e);
        }
    }

    // ========== MÉTODOS PRIVADOS DE UTILIDAD ==========

    private double calcularPorcentaje(int parte, int total) {
        if (total == 0) return 0.0;
        return ((double) parte / total) * 100.0;
    }

    private String encontrarCategoriaMasProductos(Map<String, Integer> productosPorCategoria) {
        return productosPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private String encontrarCategoriaMasValiosa(Map<String, BigDecimal> valorPorCategoria) {
        return valorPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private BigDecimal calcularImpactoEconomico(List<InventarioDTO> inventarios) {
        return inventarios.stream()
                .map(InventarioDTO::calcularValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> calcularValorPorCategoria() {
        try {
            List<Map<String, Object>> estadisticas = dataServiceClient.obtenerEstadisticasCategorias();
            
            return estadisticas.stream()
                    .collect(Collectors.toMap(
                            stat -> (String) stat.get("nombre"),
                            stat -> (BigDecimal) stat.getOrDefault("valorTotalInventario", BigDecimal.ZERO)
                    ));
        } catch (Exception e) {
            logger.warn("Error al calcular valor por categoría: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private void generarRecomendacionesStock(ReporteDTO reporte, Map<String, List<InventarioDTO>> alertasPorNivel) {
        Map<String, Object> datos = reporte.getDatos();
        if (datos == null) {
            datos = new HashMap<>();
            reporte.setDatos(datos);
        }
        
        // Generar recomendaciones basadas en reglas de negocio
        Map<String, String> recomendaciones = new HashMap<>();
        
        int sinStock = alertasPorNivel.get("SIN_STOCK").size();
        int critico = alertasPorNivel.get("CRITICO").size();
        int bajo = alertasPorNivel.get("BAJO").size();
        
        if (sinStock > 0) {
            recomendaciones.put("urgente", 
                "Reabastecer inmediatamente " + sinStock + " productos sin stock");
        }
        
        if (critico > 0) {
            recomendaciones.put("critico", 
                "Planificar reabastecimiento para " + critico + " productos con stock crítico");
        }
        
        if (bajo > 0) {
            recomendaciones.put("preventivo", 
                "Monitorear " + bajo + " productos con stock bajo");
        }
        
        datos.put("recomendaciones", recomendaciones);
    }
}
