package com.eci.secureapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para HelloController.
 * Verifica los endpoints /api/hello y /api/status.
 * 
 * @author Angel Cuervo
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloEndpointReturnsSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello from Secure Server!"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.protocol").value("HTTPS/TLS"));
    }

    @Test
    void helloEndpointReturnsTimestamp() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void statusEndpointReturnsRunning() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.secure").value(true))
                .andExpect(jsonPath("$.server").value("Spring Boot + TLS"));
    }

    @Test
    void statusEndpointReturnsThreadName() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thread").isNotEmpty());
    }
}
