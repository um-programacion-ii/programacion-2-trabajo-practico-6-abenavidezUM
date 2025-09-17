package com.microservices.dataservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests básicos de integración para el microservicio data-service
 * 
 * @author Agustin Benavidez
 */
@SpringBootTest
@ActiveProfiles("test")
class DataServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test básico para verificar que el contexto de Spring se carga correctamente
    }
}
