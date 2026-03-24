package com.eci.secureapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad de la aplicación.
 * 
 * - BCryptPasswordEncoder para hash seguro de contraseñas
 * - Rutas públicas: login, recursos estáticos, API hello
 * - CORS habilitado para permitir requests desde Apache (servidor separado)
 * 
 * @author Angel Cuervo
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean de BCryptPasswordEncoder para codificar y verificar contraseñas.
     * BCrypt incluye un salt automático, haciendo cada hash único.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de la cadena de filtros de seguridad.
     * Permite acceso público a recursos estáticos y endpoints de autenticación.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitado para API REST
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(authz -> authz
                        // Rutas públicas
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/app.html",
                                "/css/**",
                                "/js/**",
                                "/api/login",
                                "/api/hello",
                                "/api/secure-read",
                                "/api/status")
                        .permitAll()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated());

        return http.build();
    }

    /**
     * Configuración CORS para permitir requests desde el servidor Apache.
     * Necesario cuando el cliente HTML se sirve desde un servidor diferente.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
