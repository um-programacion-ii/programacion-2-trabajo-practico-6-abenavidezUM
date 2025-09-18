package com.microservices.businessservice.service;

import com.microservices.businessservice.client.DataServiceClient;
import com.microservices.businessservice.dto.ProductoDTO;
import com.microservices.businessservice.dto.ProductoRequest;
import com.microservices.businessservice.exception.BusinessException;
import com.microservices.businessservice.exception.ServiceUnavailableException;
import com.microservices.businessservice.exception.ValidationException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de negocio para la gestión de productos
 * 
 * @author Agustin Benavidez
 */
@Service
public class ProductoBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoBusinessService.class);

    private final DataServiceClient dataServiceClient;

    @Autowired
    public ProductoBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    /**
     * Obtiene todos los productos aplicando reglas de negocio
     */
    public List<ProductoDTO> obtenerTodosLosProductos() {
        logger.info("Obteniendo todos los productos");
        
        try {
            List<ProductoDTO> productos = dataServiceClient.obtenerTodosLosProductos();
            
            // Aplicar lógica de negocio adicional
            productos.forEach(this::enriquecerProducto);
            
            logger.info("Se obtuvieron {} productos", productos.size());
            return productos;
            
        } catch (FeignException e) {
            logger.error("Error al comunicarse con data-service: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "obtenerTodosLosProductos", e);
        }
    }

    /**
     * Obtiene un producto por ID con validaciones de negocio
     */
    public ProductoDTO obtenerProductoPorId(Long id) {
        logger.info("Obteniendo producto con ID: {}", id);
        
        validarId(id);
        
        try {
            ProductoDTO producto = dataServiceClient.obtenerProductoPorId(id);
            
            if (producto == null) {
                throw new BusinessException("PRODUCTO_NO_ENCONTRADO", 
                    "Producto no encontrado con ID: " + id, id);
            }
            
            enriquecerProducto(producto);
            return producto;
            
        } catch (FeignException.NotFound e) {
            throw new BusinessException("PRODUCTO_NO_ENCONTRADO", 
                "Producto no encontrado con ID: " + id, id);
        } catch (FeignException e) {
            logger.error("Error al comunicarse con data-service: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "obtenerProductoPorId", e);
        }
    }

    /**
     * Crea un nuevo producto con validaciones de negocio
     */
    public ProductoDTO crearProducto(ProductoRequest request) {
        logger.info("Creando nuevo producto: {}", request.getNombre());
        
        validarProductoRequest(request);
        
        try {
            // Convertir request a DTO
            ProductoDTO producto = convertirRequestADTO(request);
            
            // Crear el producto
            ProductoDTO productoCreado = dataServiceClient.crearProducto(
                producto, request.getStockInicial(), request.getStockMinimo());
            
            if (productoCreado == null) {
                throw new BusinessException("ERROR_CREACION", 
                    "No se pudo crear el producto: " + request.getNombre());
            }
            
            enriquecerProducto(productoCreado);
            
            logger.info("Producto creado exitosamente con ID: {}", productoCreado.getId());
            return productoCreado;
            
        } catch (FeignException e) {
            logger.error("Error al crear producto: {}", e.getMessage());
            if (e.status() == 409) {
                throw new BusinessException("PRODUCTO_DUPLICADO", 
                    "Ya existe un producto con el nombre: " + request.getNombre());
            }
            throw new ServiceUnavailableException("data-service", "crearProducto", e);
        }
    }

    /**
     * Actualiza un producto existente
     */
    public ProductoDTO actualizarProducto(Long id, ProductoRequest request) {
        logger.info("Actualizando producto con ID: {}", id);
        
        validarId(id);
        validarProductoRequest(request);
        
        try {
            // Convertir request a DTO
            ProductoDTO producto = convertirRequestADTO(request);
            producto.setId(id);
            
            ProductoDTO productoActualizado = dataServiceClient.actualizarProducto(id, producto);
            
            if (productoActualizado == null) {
                throw new BusinessException("ERROR_ACTUALIZACION", 
                    "No se pudo actualizar el producto con ID: " + id);
            }
            
            enriquecerProducto(productoActualizado);
            
            logger.info("Producto actualizado exitosamente: {}", productoActualizado.getNombre());
            return productoActualizado;
            
        } catch (FeignException.NotFound e) {
            throw new BusinessException("PRODUCTO_NO_ENCONTRADO", 
                "Producto no encontrado con ID: " + id, id);
        } catch (FeignException e) {
            logger.error("Error al actualizar producto: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "actualizarProducto", e);
        }
    }

    /**
     * Elimina un producto (soft delete)
     */
    public void eliminarProducto(Long id) {
        logger.info("Eliminando producto con ID: {}", id);
        
        validarId(id);
        
        try {
            dataServiceClient.eliminarProducto(id);
            logger.info("Producto eliminado exitosamente con ID: {}", id);
            
        } catch (FeignException.NotFound e) {
            throw new BusinessException("PRODUCTO_NO_ENCONTRADO", 
                "Producto no encontrado con ID: " + id, id);
        } catch (FeignException e) {
            logger.error("Error al eliminar producto: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "eliminarProducto", e);
        }
    }

    /**
     * Busca productos por categoría
     */
    public List<ProductoDTO> obtenerProductosPorCategoria(String categoriaNombre) {
        logger.info("Obteniendo productos de la categoría: {}", categoriaNombre);
        
        if (categoriaNombre == null || categoriaNombre.trim().isEmpty()) {
            throw new ValidationException("El nombre de la categoría es obligatorio");
        }
        
        try {
            List<ProductoDTO> productos = dataServiceClient.obtenerProductosPorCategoria(categoriaNombre);
            productos.forEach(this::enriquecerProducto);
            
            logger.info("Se encontraron {} productos en la categoría: {}", productos.size(), categoriaNombre);
            return productos;
            
        } catch (FeignException e) {
            logger.error("Error al buscar productos por categoría: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "obtenerProductosPorCategoria", e);
        }
    }

    /**
     * Busca productos por texto
     */
    public List<ProductoDTO> buscarProductos(String texto) {
        logger.info("Buscando productos con texto: {}", texto);
        
        if (texto == null || texto.trim().isEmpty()) {
            throw new ValidationException("El texto de búsqueda es obligatorio");
        }
        
        if (texto.trim().length() < 2) {
            throw new ValidationException("El texto de búsqueda debe tener al menos 2 caracteres");
        }
        
        try {
            List<ProductoDTO> productos = dataServiceClient.buscarProductos(texto);
            productos.forEach(this::enriquecerProducto);
            
            logger.info("Se encontraron {} productos con el texto: {}", productos.size(), texto);
            return productos;
            
        } catch (FeignException e) {
            logger.error("Error al buscar productos: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "buscarProductos", e);
        }
    }

    /**
     * Busca productos por rango de precios
     */
    public List<ProductoDTO> buscarProductosPorPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        logger.info("Buscando productos con precio entre {} y {}", precioMin, precioMax);
        
        validarRangoPrecios(precioMin, precioMax);
        
        try {
            List<ProductoDTO> productos = dataServiceClient.buscarProductosPorPrecio(precioMin, precioMax);
            productos.forEach(this::enriquecerProducto);
            
            logger.info("Se encontraron {} productos en el rango de precios", productos.size());
            return productos;
            
        } catch (FeignException e) {
            logger.error("Error al buscar productos por precio: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "buscarProductosPorPrecio", e);
        }
    }

    /**
     * Obtiene productos con stock bajo aplicando reglas de negocio
     */
    public List<ProductoDTO> obtenerProductosConStockBajo() {
        logger.info("Obteniendo productos con stock bajo");
        
        try {
            List<ProductoDTO> productos = dataServiceClient.obtenerProductosConStockBajo();
            productos.forEach(this::enriquecerProducto);
            
            // Agregar prioridad de reabastecimiento
            productos.forEach(this::calcularPrioridadReabastecimiento);
            
            logger.warn("ALERTA: {} productos tienen stock bajo", productos.size());
            return productos;
            
        } catch (FeignException e) {
            logger.error("Error al obtener productos con stock bajo: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "obtenerProductosConStockBajo", e);
        }
    }

    /**
     * Calcula el valor total del inventario
     */
    public BigDecimal calcularValorTotalInventario() {
        logger.info("Calculando valor total del inventario");
        
        try {
            BigDecimal valorTotal = dataServiceClient.obtenerValorTotalInventario();
            logger.info("Valor total del inventario: {}", valorTotal);
            return valorTotal;
            
        } catch (FeignException e) {
            logger.error("Error al calcular valor total del inventario: {}", e.getMessage());
            throw new ServiceUnavailableException("data-service", "obtenerValorTotalInventario", e);
        }
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN Y UTILIDAD ==========

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("El ID debe ser un número positivo");
        }
    }

    private void validarProductoRequest(ProductoRequest request) {
        Map<String, String> errores = new HashMap<>();
        
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            errores.put("nombre", "El nombre es obligatorio");
        } else if (request.getNombre().trim().length() < 2) {
            errores.put("nombre", "El nombre debe tener al menos 2 caracteres");
        }
        
        if (request.getPrecio() == null || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            errores.put("precio", "El precio debe ser mayor a cero");
        }
        
        if (request.getCategoriaId() == null || request.getCategoriaId() <= 0) {
            errores.put("categoriaId", "La categoría es obligatoria");
        }
        
        if (request.getStockInicial() != null && request.getStockInicial() < 0) {
            errores.put("stockInicial", "El stock inicial no puede ser negativo");
        }
        
        if (request.getStockMinimo() != null && request.getStockMinimo() < 0) {
            errores.put("stockMinimo", "El stock mínimo no puede ser negativo");
        }
        
        if (!errores.isEmpty()) {
            throw new ValidationException("Errores de validación en el producto", errores);
        }
    }

    private void validarRangoPrecios(BigDecimal precioMin, BigDecimal precioMax) {
        if (precioMin == null || precioMax == null) {
            throw new ValidationException("Los precios mínimo y máximo son obligatorios");
        }
        
        if (precioMin.compareTo(BigDecimal.ZERO) < 0 || precioMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Los precios no pueden ser negativos");
        }
        
        if (precioMin.compareTo(precioMax) > 0) {
            throw new ValidationException("El precio mínimo no puede ser mayor al precio máximo");
        }
    }

    private ProductoDTO convertirRequestADTO(ProductoRequest request) {
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre(request.getNombre());
        dto.setDescripcion(request.getDescripcion());
        dto.setPrecio(request.getPrecio());
        dto.setCategoriaId(request.getCategoriaId());
        dto.setActivo(true);
        return dto;
    }

    private void enriquecerProducto(ProductoDTO producto) {
        if (producto == null) return;
        
        // Calcular valor del inventario
        if (producto.getPrecio() != null && producto.getStock() != null) {
            BigDecimal valorInventario = producto.getPrecio().multiply(BigDecimal.valueOf(producto.getStock()));
            producto.setValorInventario(valorInventario);
        }
        
        // Determinar estado del stock
        producto.setEstadoStock(producto.getEstadoStockCalculado());
    }

    private void calcularPrioridadReabastecimiento(ProductoDTO producto) {
        // Lógica de negocio para calcular prioridad de reabastecimiento
        // Esto es un ejemplo de cómo el business service puede agregar valor
        if (producto.getStock() != null && producto.getStockMinimo() != null) {
            double porcentajeStock = (double) producto.getStock() / producto.getStockMinimo();
            if (porcentajeStock <= 0.2) {
                // Stock crítico
                logger.warn("CRÍTICO: Producto '{}' tiene stock crítico ({}/{})", 
                           producto.getNombre(), producto.getStock(), producto.getStockMinimo());
            }
        }
    }
}
