package com.microservices.businessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Microservicio de Negocio para Sistema de Gestión de Productos
 * 
 * Este microservicio se encarga de la lógica de negocio, validaciones
 * y comunicación con el microservicio de datos a través de Feign.
 * 
 * @author Agustin Benavidez
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableFeignClients
public class BusinessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessServiceApplication.class, args);
    }
}
