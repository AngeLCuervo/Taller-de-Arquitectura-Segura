package com.eci.secureapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Clase principal de la aplicación SecureApp.
 * 
 * Características:
 * - Soporte para solicitudes concurrentes mediante ThreadPoolTaskExecutor
 * - Apagado elegante (graceful shutdown) mediante un Shutdown Hook en Runtime
 * 
 * @author Angel Cuervo
 */
@SpringBootApplication
@EnableAsync
public class SecureAppApplication {

    private static final Logger logger = LoggerFactory.getLogger(SecureAppApplication.class);
    private static volatile boolean running = true;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SecureAppApplication.class, args);

        // Shutdown Hook: se activa cuando la JVM recibe una señal de terminación (Ctrl+C, kill, etc.)
        // Referencia: https://www.baeldung.com/jvm-shutdown-hooks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("========================================");
            logger.info("Shutdown Hook activado - Apagando el servidor de forma elegante...");
            logger.info("========================================");

            // Terminar la condición del ciclo
            running = false;

            // Cerrar el contexto de Spring de forma ordenada
            if (context.isActive()) {
                context.close();
                logger.info("Contexto de Spring cerrado correctamente.");
            }

            logger.info("Servidor apagado exitosamente. ¡Hasta luego!");
        }));

        logger.info("========================================");
        logger.info("SecureApp iniciada correctamente.");
        logger.info("Servidor escuchando en el puerto configurado con HTTPS.");
        logger.info("Shutdown Hook registrado - El servidor se apagará de forma elegante.");
        logger.info("========================================");
    }

    /**
     * Configura un pool de hilos para manejar solicitudes concurrentes.
     * Esto permite que múltiples clientes realicen peticiones simultáneamente.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("SecureApp-Async-");
        executor.initialize();
        logger.info("ThreadPoolTaskExecutor configurado: corePoolSize=4, maxPoolSize=10, queueCapacity=25");
        return executor;
    }

    /**
     * Indica si el servidor sigue en ejecución.
     * El Shutdown Hook cambia este valor a false para terminar ciclos de forma elegante.
     */
    public static boolean isRunning() {
        return running;
    }
}
