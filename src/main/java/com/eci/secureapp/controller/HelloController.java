package com.eci.secureapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador REST para el servicio Hello.
 * Endpoint seguro servido sobre HTTPS.
 * 
 * @author Angel Cuervo
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    /**
     * Endpoint GET /api/hello
     * Retorna un mensaje de saludo en formato JSON.
     * Accesible públicamente sobre HTTPS.
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        logger.info("Solicitud recibida en /api/hello");

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Hello from Secure Server!");
        response.put("status", "success");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("protocol", "HTTPS/TLS");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint GET /api/status
     * Retorna el estado del servidor.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        logger.info("Solicitud recibida en /api/status");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "running");
        response.put("secure", true);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("server", "Spring Boot + TLS");
        response.put("thread", Thread.currentThread().getName());

        return ResponseEntity.ok(response);
    }
}
