package com.eci.secureapp.service;

import com.eci.secureapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de usuarios para autenticación.
 * 
 * Almacena usuarios con contraseñas hasheadas con BCrypt.
 * Las contraseñas NUNCA se almacenan en texto plano.
 * 
 * @author Angel Cuervo
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder;
    private final Map<String, User> users = new HashMap<>();

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Inicializa usuarios de demostración con contraseñas hasheadas.
     * En producción, estos datos vendrían de una base de datos.
     */
    @PostConstruct
    public void initUsers() {
        // Crear usuarios de demostración con contraseñas hasheadas
        addUser("admin", "admin123");
        addUser("angel", "secure456");
        addUser("user", "password789");
        logger.info("Usuarios de demostración inicializados con contraseñas BCrypt-hasheadas.");
    }

    /**
     * Agrega un nuevo usuario con su contraseña hasheada.
     * 
     * @param username nombre de usuario
     * @param rawPassword contraseña en texto plano (será hasheada con BCrypt)
     */
    public void addUser(String username, String rawPassword) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        users.put(username, new User(username, hashedPassword));
        logger.debug("Usuario '{}' creado con contraseña hasheada.", username);
    }

    /**
     * Autentica un usuario verificando su contraseña contra el hash almacenado.
     * 
     * @param username nombre de usuario
     * @param rawPassword contraseña en texto plano a verificar
     * @return true si las credenciales son válidas, false en caso contrario
     */
    public boolean authenticate(String username, String rawPassword) {
        User user = users.get(username);
        if (user == null) {
            logger.warn("Intento de login fallido: usuario '{}' no existe.", username);
            return false;
        }

        boolean matches = passwordEncoder.matches(rawPassword, user.getHashedPassword());
        if (matches) {
            logger.info("Login exitoso para usuario '{}'.", username);
        } else {
            logger.warn("Intento de login fallido: contraseña incorrecta para usuario '{}'.", username);
        }
        return matches;
    }

    /**
     * Verifica si un usuario existe.
     * 
     * @param username nombre de usuario
     * @return true si el usuario existe
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Obtiene el hash de la contraseña de un usuario (para pruebas).
     * 
     * @param username nombre de usuario
     * @return hash BCrypt de la contraseña, o null si no existe
     */
    public String getPasswordHash(String username) {
        User user = users.get(username);
        return user != null ? user.getHashedPassword() : null;
    }
}
