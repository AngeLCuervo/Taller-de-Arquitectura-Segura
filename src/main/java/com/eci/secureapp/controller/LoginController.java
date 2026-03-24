package com.eci.secureapp.controller;

import com.eci.secureapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador REST para autenticación de usuarios.
 * Implementa login con verificación de contraseñas hasheadas con BCrypt.
 * 
 * @author Angel Cuervo
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint POST /api/login
     * Autentica un usuario verificando su contraseña contra el hash BCrypt almacenado.
     * 
     * @param credentials Map con "username" y "password"
     * @return 200 OK si las credenciales son válidas, 401 Unauthorized si no lo son
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        logger.info("Intento de login para usuario: {}", username);

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Usuario y contraseña son requeridos.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        boolean authenticated = userService.authenticate(username, password);

        Map<String, Object> response = new LinkedHashMap<>();
        if (authenticated) {
            response.put("success", true);
            response.put("message", "Login exitoso. ¡Bienvenido, " + username + "!");
            response.put("username", username);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Credenciales inválidas. Verifique usuario y contraseña.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
