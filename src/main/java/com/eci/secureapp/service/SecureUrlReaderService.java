package com.eci.secureapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para leer URLs HTTPS de forma segura usando un TrustStore personalizado.
 * 
 * Configura SSLContext con un TrustStore PKCS12 para verificar certificados
 * de servidores remotos. Soporta lectura asíncrona para concurrencia.
 * 
 * @author Angel Cuervo
 */
@Service
public class SecureUrlReaderService {

    private static final Logger logger = LoggerFactory.getLogger(SecureUrlReaderService.class);

    /**
     * Lee el contenido de una URL HTTPS usando el TrustStore del sistema.
     * 
     * @param urlString URL HTTPS a leer
     * @return contenido de la URL como String
     */
    public String readUrl(String urlString) {
        logger.info("Leyendo URL segura: {}", urlString);
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            logger.info("URL leída exitosamente: {} ({} caracteres)", urlString, content.length());
        } catch (Exception e) {
            String errorMsg = "Error leyendo URL " + urlString + ": " + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        }

        return content.toString();
    }

    /**
     * Lee una URL HTTPS de forma asíncrona (soporte concurrente).
     * Usa el ThreadPoolTaskExecutor configurado en SecureAppApplication.
     * 
     * @param urlString URL HTTPS a leer
     * @return CompletableFuture con el contenido de la URL
     */
    @Async("taskExecutor")
    public CompletableFuture<String> readUrlAsync(String urlString) {
        String result = readUrl(urlString);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Lee una URL HTTPS usando un TrustStore personalizado.
     * Esto permite verificar certificados autofirmados de servidores propios.
     * 
     * @param urlString URL HTTPS a leer
     * @param trustStorePath ruta al archivo TrustStore (PKCS12)
     * @param trustStorePassword contraseña del TrustStore
     * @return contenido de la URL como String
     */
    public String readUrlWithTrustStore(String urlString, String trustStorePath, String trustStorePassword) {
        logger.info("Leyendo URL segura con TrustStore personalizado: {}", urlString);

        try {
            // Cargar el TrustStore
            File trustStoreFile = new File(trustStorePath);
            if (!trustStoreFile.exists()) {
                return "Error: TrustStore no encontrado en " + trustStorePath;
            }

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream fis = new FileInputStream(trustStoreFile)) {
                trustStore.load(fis, trustStorePassword.toCharArray());
            }

            // Crear TrustManagerFactory con el TrustStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // Crear SSLContext con los TrustManagers
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            // Configurar la conexión con el SSLContext personalizado
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            logger.info("URL leída exitosamente con TrustStore: {} ({} caracteres)", 
                        urlString, content.length());
            return content.toString();

        } catch (Exception e) {
            String errorMsg = "Error leyendo URL con TrustStore " + urlString + ": " + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        }
    }
}
