package com.eci.secureapp.model;

/**
 * Modelo de usuario para la autenticación.
 * Almacena nombre de usuario y contraseña hasheada con BCrypt.
 * 
 * @author Angel Cuervo
 */
public class User {

    private String username;
    private String hashedPassword;

    public User() {
    }

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String toString() {
        return "User{username='" + username + "'}";
    }
}
