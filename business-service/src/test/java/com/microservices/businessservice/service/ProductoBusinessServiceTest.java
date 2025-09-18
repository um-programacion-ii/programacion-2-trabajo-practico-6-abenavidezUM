package com.microservices.businessservice.service;

import com.microservices.businessservice.client.DataServiceClient;
import com.microservices.businessservice.dto.ProductoDTO;
import com.microservices.businessservice.dto.ProductoRequest;
import com.microservices.businessservice.exception.ValidationException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductoBusinessService
 * 
 * @author Agustin Benavidez
 */
@ExtendWith(MockitoExtension.class)
class ProductoBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private ProductoBusinessService productoBusinessService;

    private ProductoDTO productoEjemplo;
    private ProductoRequest requestEjemplo;

    @BeforeEach
    void setUp() {
        productoEjemplo = new ProductoDTO(1L, "Producto Test", "Descripción test", 
                                        BigDecimal.valueOf(100), "Categoría Test", 10, false);
        
        requestEjemplo = new ProductoRequest("Producto Nuevo", "Descripción nueva", 
                                           BigDecimal.valueOf(200), 1L, 15, 5);
    }

    @Test
    void cuandoObtenerTodosLosProductos_entoncesRetornaListaEnriquecida() {
        // Arrange
        List<ProductoDTO> productosEsperados = Arrays.asList(productoEjemplo);
        when(dataServiceClient.obtenerTodosLosProductos()).thenReturn(productosEsperados);

        // Act
        List<ProductoDTO> resultado = productoBusinessService.obtenerTodosLosProductos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Producto Test", resultado.get(0).getNombre());
        assertNotNull(resultado.get(0).getValorInventario());
        assertNotNull(resultado.get(0).getEstadoStock());
        verify(dataServiceClient).obtenerTodosLosProductos();
    }

    @Test
    void cuandoObtenerProductoPorId_conIdValido_entoncesRetornaProductoEnriquecido() {
        // Arrange
        Long id = 1L;
        when(dataServiceClient.obtenerProductoPorId(id)).thenReturn(productoEjemplo);

        // Act
        ProductoDTO resultado = productoBusinessService.obtenerProductoPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals("Producto Test", resultado.getNombre());
        assertNotNull(resultado.getValorInventario());
        verify(dataServiceClient).obtenerProductoPorId(id);
    }

    @Test
    void cuandoObtenerProductoPorId_conIdInvalido_entoncesLanzaValidationException() {
        // Arrange
        Long idInvalido = -1L;

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            productoBusinessService.obtenerProductoPorId(idInvalido);
        });

        verify(dataServiceClient, never()).obtenerProductoPorId(any());
    }

    @Test
    void cuandoCrearProducto_conDatosValidos_entoncesRetornaProductoCreado() {
        // Arrange
        when(dataServiceClient.crearProducto(any(ProductoDTO.class), eq(15), eq(5)))
            .thenReturn(productoEjemplo);

        // Act
        ProductoDTO resultado = productoBusinessService.crearProducto(requestEjemplo);

        // Assert
        assertNotNull(resultado);
        assertEquals("Producto Test", resultado.getNombre());
        verify(dataServiceClient).crearProducto(any(ProductoDTO.class), eq(15), eq(5));
    }

    @Test
    void cuandoCrearProducto_conNombreVacio_entoncesLanzaValidationException() {
        // Arrange
        requestEjemplo.setNombre("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            productoBusinessService.crearProducto(requestEjemplo);
        });

        verify(dataServiceClient, never()).crearProducto(any(), any(), any());
    }

    @Test
    void cuandoCrearProducto_conPrecioNegativo_entoncesLanzaValidationException() {
        // Arrange
        requestEjemplo.setPrecio(BigDecimal.valueOf(-10));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            productoBusinessService.crearProducto(requestEjemplo);
        });

        verify(dataServiceClient, never()).crearProducto(any(), any(), any());
    }

    @Test
    void cuandoBuscarProductos_conTextoValido_entoncesRetornaResultados() {
        // Arrange
        String texto = "test";
        List<ProductoDTO> productosEsperados = Arrays.asList(productoEjemplo);
        when(dataServiceClient.buscarProductos(texto)).thenReturn(productosEsperados);

        // Act
        List<ProductoDTO> resultado = productoBusinessService.buscarProductos(texto);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(dataServiceClient).buscarProductos(texto);
    }

    @Test
    void cuandoBuscarProductos_conTextoCorto_entoncesLanzaValidationException() {
        // Arrange
        String textoCorto = "a";

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            productoBusinessService.buscarProductos(textoCorto);
        });

        verify(dataServiceClient, never()).buscarProductos(any());
    }

    @Test
    void cuandoBuscarProductosPorPrecio_conRangoValido_entoncesRetornaResultados() {
        // Arrange
        BigDecimal min = BigDecimal.valueOf(50);
        BigDecimal max = BigDecimal.valueOf(150);
        List<ProductoDTO> productosEsperados = Arrays.asList(productoEjemplo);
        when(dataServiceClient.buscarProductosPorPrecio(min, max)).thenReturn(productosEsperados);

        // Act
        List<ProductoDTO> resultado = productoBusinessService.buscarProductosPorPrecio(min, max);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(dataServiceClient).buscarProductosPorPrecio(min, max);
    }

    @Test
    void cuandoBuscarProductosPorPrecio_conRangoInvalido_entoncesLanzaValidationException() {
        // Arrange
        BigDecimal min = BigDecimal.valueOf(150);
        BigDecimal max = BigDecimal.valueOf(50); // min > max

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            productoBusinessService.buscarProductosPorPrecio(min, max);
        });

        verify(dataServiceClient, never()).buscarProductosPorPrecio(any(), any());
    }

    @Test
    void cuandoDataServiceNoDisponible_entoncesLanzaServiceUnavailableException() {
        // Arrange
        when(dataServiceClient.obtenerTodosLosProductos())
            .thenThrow(FeignException.InternalServerError.class);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            productoBusinessService.obtenerTodosLosProductos();
        });

        verify(dataServiceClient).obtenerTodosLosProductos();
    }
}
