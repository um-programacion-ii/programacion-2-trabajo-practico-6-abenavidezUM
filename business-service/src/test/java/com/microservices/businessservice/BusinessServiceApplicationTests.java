package com.microservices.businessservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests básicos de integración para el microservicio business-service
 * 
 * @author Agustin Benavidez
 */
@SpringBootTest
@ActiveProfiles("test")
class BusinessServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test básico para verificar que el contexto de Spring se carga correctamente
        // incluyendo la configuración de Feign
    }
}
