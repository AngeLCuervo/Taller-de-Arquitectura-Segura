package com.eci.secureapp.controller;

import com.eci.secureapp.service.SecureUrlReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST para lectura segura de URLs HTTPS.
 * Demuestra el uso de SSLContext y TrustStore personalizados.
 * 
 * @author Angel Cuervo
 */
@RestController
@RequestMapping("/api")
public class SecureUrlReaderController {

    private static final Logger logger = LoggerFactory.getLogger(SecureUrlReaderController.class);
    private final SecureUrlReaderService secureUrlReaderService;

    public SecureUrlReaderController(SecureUrlReaderService secureUrlReaderService) {
        this.secureUrlReaderService = secureUrlReaderService;
    }

    /**
     * Endpoint GET /api/secure-read
     * Lee una URL HTTPS de forma segura y retorna su contenido.
     * 
     * @param url URL HTTPS a leer
     * @return Contenido de la URL en formato JSON
     */
    @GetMapping("/secure-read")
    public ResponseEntity<Map<String, Object>> readSecureUrl(
            @RequestParam(defaultValue = "https://www.google.com") String url) {

        logger.info("Solicitud de lectura segura para URL: {}", url);

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Lectura asíncrona para demostrar concurrencia
            CompletableFuture<String> futureContent = secureUrlReaderService.readUrlAsync(url);
            String content = futureContent.get(); // Esperar resultado

            response.put("success", true);
            response.put("url", url);
            response.put("contentLength", content.length());
            // Limitar el contenido a los primeros 1000 caracteres para la respuesta
            response.put("contentPreview", content.length() > 1000 
                    ? content.substring(0, 1000) + "..." 
                    : content);
            response.put("thread", Thread.currentThread().getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error leyendo URL segura: {}", url, e);
            response.put("success", false);
            response.put("url", url);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
