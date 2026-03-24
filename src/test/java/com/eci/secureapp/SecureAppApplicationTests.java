package com.eci.secureapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test que verifica que el contexto de Spring Boot carga correctamente.
 * 
 * @author Angel Cuervo
 */
@SpringBootTest
@ActiveProfiles("test")
class SecureAppApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring carga correctamente
        // Si hay errores de configuración, este test fallará
    }
}
