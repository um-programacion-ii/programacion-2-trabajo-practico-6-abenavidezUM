package com.microservices.dataservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dataservice.entity.Categoria;
import com.microservices.dataservice.entity.Producto;
import com.microservices.dataservice.service.CategoriaService;
import com.microservices.dataservice.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para DataController
 * 
 * @author Agustin Benavidez
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DataControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Categoria categoriaTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Crear categoría de prueba
        categoriaTest = new Categoria("Electrónicos Test", "Categoría para testing");
        categoriaTest = categoriaService.crear(categoriaTest);
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/data/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("data-service"));
    }

    @Test
    void testObtenerTodasLasCategorias() throws Exception {
        mockMvc.perform(get("/data/categorias"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$").isArray());
    }

    @Test
    void testCrearCategoria() throws Exception {
        Categoria nuevaCategoria = new Categoria("Ropa Test", "Categoría de ropa para testing");
        
        mockMvc.perform(post("/data/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaCategoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ropa Test"))
                .andExpect(jsonPath("$.descripcion").value("Categoría de ropa para testing"));
    }

    @Test
    void testCrearProducto() throws Exception {
        Producto producto = new Producto("Smartphone Test", "Teléfono para testing", 
                                       new BigDecimal("50000.00"), categoriaTest);
        
        mockMvc.perform(post("/data/productos?cantidadInicial=10&stockMinimo=5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Smartphone Test"))
                .andExpect(jsonPath("$.precio").value(50000.00));
    }

    @Test
    void testObtenerProductosPorCategoria() throws Exception {
        // Crear un producto en la categoría de prueba
        Producto producto = new Producto("Laptop Test", "Laptop para testing", 
                                       new BigDecimal("75000.00"), categoriaTest);
        productoService.crear(producto, 5, 2);
        
        mockMvc.perform(get("/data/productos/categoria/" + categoriaTest.getNombre()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testBuscarProductosInexistentes() throws Exception {
        mockMvc.perform(get("/data/productos/buscar?texto=ProductoInexistente"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testObtenerProductoInexistente() throws Exception {
        mockMvc.perform(get("/data/productos/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }

    @Test
    void testObtenerEstadisticasInventario() throws Exception {
        mockMvc.perform(get("/data/inventario/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProductos").exists())
                .andExpect(jsonPath("$.cantidadTotalItems").exists());
    }
}
