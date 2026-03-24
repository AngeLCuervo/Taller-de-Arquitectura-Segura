package com.eci.secureapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UserService.
 * Verifica el hashing BCrypt y la autenticación de usuarios.
 * 
 * @author Angel Cuervo
 */
class UserServiceTest {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(passwordEncoder);
        userService.initUsers();
    }

    @Test
    void authenticateWithValidCredentialsReturnsTrue() {
        // Las contraseñas fueron inicializadas en initUsers()
        assertTrue(userService.authenticate("admin", "admin123"));
        assertTrue(userService.authenticate("angel", "secure456"));
        assertTrue(userService.authenticate("user", "password789"));
    }

    @Test
    void authenticateWithInvalidPasswordReturnsFalse() {
        assertFalse(userService.authenticate("admin", "wrongpassword"));
        assertFalse(userService.authenticate("angel", "notmypassword"));
    }

    @Test
    void authenticateWithNonExistentUserReturnsFalse() {
        assertFalse(userService.authenticate("noexiste", "anypassword"));
    }

    @Test
    void passwordsAreStoredAsHashes() {
        String hash = userService.getPasswordHash("admin");
        assertNotNull(hash);
        // BCrypt hashes empiezan con "$2a$" o "$2b$"
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"),
                "El hash debe ser formato BCrypt");
        // El hash NO debe ser la contraseña en texto plano
        assertNotEquals("admin123", hash);
    }

    @Test
    void samePlaintextProducesDifferentHashes() {
        // BCrypt genera un salt aleatorio cada vez
        String hash1 = passwordEncoder.encode("admin123");
        String hash2 = passwordEncoder.encode("admin123");
        // Los hashes deben ser diferentes (salt diferente)
        assertNotEquals(hash1, hash2, "Cada hash BCrypt debe ser único debido al salt");
        // Pero ambos deben validar contra la misma contraseña
        assertTrue(passwordEncoder.matches("admin123", hash1));
        assertTrue(passwordEncoder.matches("admin123", hash2));
    }

    @Test
    void userExistsReturnsTrueForExistingUser() {
        assertTrue(userService.userExists("admin"));
        assertTrue(userService.userExists("angel"));
        assertTrue(userService.userExists("user"));
    }

    @Test
    void userExistsReturnsFalseForNonExistentUser() {
        assertFalse(userService.userExists("noexiste"));
        assertFalse(userService.userExists(""));
    }

    @Test
    void addUserAndAuthenticate() {
        userService.addUser("newuser", "newpass123");
        assertTrue(userService.userExists("newuser"));
        assertTrue(userService.authenticate("newuser", "newpass123"));
        assertFalse(userService.authenticate("newuser", "wrongpass"));
    }
}
