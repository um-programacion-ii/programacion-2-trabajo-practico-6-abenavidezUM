package com.microservices.dataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Datos para Sistema de Gestión de Productos
 * 
 * Este microservicio se encarga de la persistencia de datos y operaciones CRUD
 * para las entidades Producto, Categoria e Inventario.
 * 
 * @author Agustin Benavidez
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
public class DataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataServiceApplication.class, args);
    }
}
