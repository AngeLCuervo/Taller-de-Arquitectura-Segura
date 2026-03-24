#  Taller de Arquitectura Segura — SecureApp

**Programador:** Angel Cuervo  
**Materia:** Arquitectura Empresarial (AREP)  
**Escuela:** Escuela Colombiana de Ingeniería Julio Garavito  
**Fecha:** Marzo 2026

---

##  Descripción

Aplicación web segura construida con **Spring Boot**, que implementa:

- **HTTPS/TLS** con certificados digitales (keystore PKCS12)
- **Login seguro** con contraseñas almacenadas como hashes **BCrypt**
- **Cliente asíncrono** HTML + JavaScript (Fetch API con async/await)
- **Soporte de solicitudes concurrentes** con `ThreadPoolTaskExecutor`
- **Apagado** mediante Shutdown Hook en Runtime ([ver referencia](https://www.baeldung.com/jvm-shutdown-hooks))
- **Lectura segura de URLs HTTPS** con SSLContext y TrustStore personalizado
- **Despliegue en AWS** con Apache (servidor 1) y Spring Boot (servidor 2)

---

##  Arquitectura del Sistema

La arquitectura sigue un modelo de **dos servidores** desplegados en AWS:

```
┌─────────────────────────────────────────────────────────────┐
│                        INTERNET                             │
│                     (HTTPS / TLS)                           │
└──────────┬──────────────────────────────┬───────────────────┘
           │                              │
           ▼                              ▼
┌─────────────────────┐     ┌──────────────────────────┐
│  Servidor 1 (AWS)   │     │   Servidor 2 (AWS)       │
│  ┌───────────────┐  │     │  ┌────────────────────┐  │
│  │   Apache HTTP  │  │     │  │  Spring Boot App   │  │
│  │   + TLS/SSL   │  │     │  │  + Tomcat Embebido │  │
│  │               │  │     │  │  + TLS/SSL          │  │
│  │  Sirve HTML   │  │────▶│  │                    │  │
│  │  + JS + CSS   │  │HTTPS│  │  REST API:         │  │
│  │  (cliente     │  │     │  │  /api/hello         │  │
│  │   asíncrono)  │  │     │  │  /api/login         │  │
│  │               │  │     │  │  /api/secure-read   │  │
│  │  Let's Encrypt│  │     │  │  /api/status        │  │
│  └───────────────┘  │     │  └────────────────────┘  │
│  Puerto: 443        │     │  Puerto: 8443            │
│  Dominio: arepeci.  │     │  Keystore: PKCS12        │
│  duckdns.org        │     │  BCrypt passwords        │
└─────────────────────┘     └──────────────────────────┘
```

### Componentes principales:

| Componente | Tecnología | Función |
|-----------|-----------|---------|
| **Apache Server** | Apache HTTP + mod_ssl | Sirve el cliente HTML+JS sobre HTTPS con Let's Encrypt |
| **Spring Boot** | Spring Boot 3.2.4 + Tomcat | Backend REST API sobre HTTPS con keystore PKCS12 |
| **Cliente Web** | HTML5 + JavaScript (Fetch API) | Cliente asíncrono que se comunica con el backend via HTTPS |
| **Seguridad** | BCrypt + TLS + Shutdown Hook | Hashing de contraseñas, cifrado de datos, apagado elegante |

### Diagrama de flujo de autenticación:

```
┌──────────┐    HTTPS     ┌──────────────┐
│  Browser │──────────────▶│  Apache (443) │
│          │◀──────────────│  HTML+JS+CSS  │
└──────────┘               └──────────────┘
      │
      │ Fetch API (async/await)
      │ HTTPS POST /api/login
      │ {username, password}
      ▼
┌──────────────┐    BCrypt     ┌──────────────┐
│  Spring Boot │───────────────▶│  UserService  │
│  (8443/TLS)  │               │  .authenticate│
│              │               │  BCrypt.matches│
│  LoginCtrl   │◀──────────────│  (hash check) │
└──────────────┘  true/false   └──────────────┘
```

---

##  Estructura del Proyecto (Maven)

```
Taller de Arquitectura Segura/
├── pom.xml                                    # Configuración Maven
├── .gitignore                                 # Archivos ignorados
├── README.md                                  # Este archivo
├── scripts/
│   ├── generate-keystore.sh                   # Script Linux/Mac
│   └── generate-keystore.bat                  # Script Windows
├── src/
│   ├── main/
│   │   ├── java/com/eci/secureapp/
│   │   │   ├── SecureAppApplication.java      # Main + Shutdown Hook + Async
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java        # Spring Security + BCrypt
│   │   │   ├── controller/
│   │   │   │   ├── HelloController.java       # GET /api/hello, /api/status
│   │   │   │   ├── LoginController.java       # POST /api/login (BCrypt)
│   │   │   │   └── SecureUrlReaderController.java  # GET /api/secure-read
│   │   │   ├── model/
│   │   │   │   └── User.java                  # Modelo de usuario
│   │   │   └── service/
│   │   │       ├── UserService.java           # Autenticación BCrypt
│   │   │       └── SecureUrlReaderService.java # HTTPS con TrustStore
│   │   └── resources/
│   │       ├── application.properties         # Config TLS + servidor
│   │       ├── ecikeystore.p12                # Keystore PKCS12 (generado)
│   │       └── static/
│   │           ├── index.html                 # Página de login
│   │           ├── app.html                   # Panel principal
│   │           ├── css/style.css              # Estilos
│   │           └── js/app.js                  # Cliente async JS
│   └── test/
│       ├── java/com/eci/secureapp/
│       │   ├── SecureAppApplicationTests.java # Test de contexto
│       │   ├── controller/
│       │   │   ├── HelloControllerTest.java   # Tests de /api/hello
│       │   │   └── LoginControllerTest.java   # Tests de login BCrypt
│       │   └── service/
│       │       └── UserServiceTest.java       # Tests de hashing
│       └── resources/
│           └── application-test.properties    # Config sin SSL para tests
└── docs/
    └── apache-config/                         # Config Apache + Let's Encrypt
```

---

##  Características de Seguridad

### 1. TLS/HTTPS (Cifrado de Datos)
- **Keystore PKCS12** generado con `keytool` (RSA 2048 bits)
- Toda la comunicación está cifrada con TLS
- Configurado en `application.properties`:
  ```properties
  server.ssl.key-store-type=PKCS12
  server.ssl.key-store=classpath:ecikeystore.p12
  server.ssl.key-alias=ecikeypair
  ```

### 2. Login Seguro con BCrypt
- Las contraseñas **NUNCA** se almacenan en texto plano
- Se usa `BCryptPasswordEncoder` de Spring Security
- BCrypt incluye un **salt automático**, haciendo cada hash único
- Ejemplo de hash generado: `$2a$10$N9qo8uLOickgx2ZMRZoMye...`

### 3. Cliente Asíncrono (Fetch API)
- El cliente JavaScript usa `async/await` con la Fetch API
- Comunicación no-bloqueante con el servidor
- Ejemplo:
  ```javascript
  const response = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
  });
  ```

### 4. Soporte de Solicitudes Concurrentes
- `ThreadPoolTaskExecutor` con pool de hilos configurable
- `@EnableAsync` para procesamiento asíncrono en el servidor
- Configuración: `corePoolSize=4, maxPoolSize=10, queueCapacity=25`

### 5. Apagado  (Graceful Shutdown)
- Implementado con `Runtime.getRuntime().addShutdownHook()`
- Al recibir señal de terminación (Ctrl+C, kill):
  1. Cambia la condición del ciclo (`running = false`)
  2. Cierra el contexto de Spring de forma ordenada
  3. Registra logs del proceso de apagado
- Referencia: [Baeldung - JVM Shutdown Hooks](https://www.baeldung.com/jvm-shutdown-hooks)

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    logger.info("Shutdown Hook activado - Apagando el servidor...");
    running = false;
    if (context.isActive()) {
        context.close();
    }
    logger.info("Servidor apagado exitosamente.");
}));
```

### 6. Lectura Segura de URLs con TrustStore
- Servicio `SecureUrlReaderService` que configura SSLContext personalizado
- Carga TrustStore PKCS12 con `TrustManagerFactory`
- Permite verificar certificados autofirmados de servidores propios

---

##  Instalación y Ejecución

### Prerequisitos

- **Java 17** o superior
- **Maven 3.6+**
- **Git**
- **keytool** (incluido con JDK)

### 1. Clonar el Repositorio

```bash
git clone https://github.com/AngeLCuervo/Secure-Application-Desing
cd AREP-SecureApp
```

### 2. Generar el Keystore (si no existe)

**Windows:**
```cmd
scripts\generate-keystore.bat
```

**Linux/Mac:**
```bash
chmod +x scripts/generate-keystore.sh
./scripts/generate-keystore.sh
```

Este script genera:
- `src/main/resources/ecikeystore.p12` — Keystore PKCS12 con par de llaves RSA 2048
- `ecicert.cer` — Certificado exportado
- `keystores/myTrustStore.p12` — TrustStore para verificación de certificados

### 3. Compilar el Proyecto

```bash
mvn clean compile
```

### 4. Ejecutar los Tests

```bash
mvn clean test
```

### 5. Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

O con el JAR empaquetado:
```bash
mvn clean package -DskipTests
java -jar target/secure-app-1.0.0.jar
```

### 6. Acceder a la Aplicación

Abrir en el navegador: **https://localhost:8443**

>  El navegador mostrará una advertencia de certificado autofirmado. Aceptar para continuar.

**Usuarios de demostración:**

| Usuario | Contraseña |
|---------|-----------|
| admin | admin123 |
| angel | secure456 |
| user | password789 |

---

##  Evidencia de Tests

Se ejecutaron **18 tests automatizados** que cubren:

### Resultados:  BUILD SUCCESS

```
[INFO] Tests run: 4, Failures: 0 -- HelloControllerTest
[INFO] Tests run: 5, Failures: 0 -- LoginControllerTest
[INFO] Tests run: 8, Failures: 0 -- UserServiceTest
[INFO] Tests run: 1, Failures: 0 -- SecureAppApplicationTests
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Detalle de Tests:

| Clase de Test | Tests | Descripción |
|--------------|-------|-------------|
| `HelloControllerTest` | 4 | Verifica endpoints /api/hello y /api/status, respuestas JSON correctas |
| `LoginControllerTest` | 5 | Login exitoso, contraseña incorrecta, usuario inexistente, credenciales vacías, login usuario "angel" |
| `UserServiceTest` | 8 | Autenticación válida/inválida, hashes BCrypt, unicidad de salt, existencia de usuarios, creación dinámica |
| `SecureAppApplicationTests` | 1 | Carga correcta del contexto de Spring |

### Tests de Seguridad específicos:

- **`passwordsAreStoredAsHashes`**: Verifica que las contraseñas se almacenan como hash BCrypt (comienzan con `$2a$`)
- **`samePlaintextProducesDifferentHashes`**: Verifica que el mismo texto plano genera hashes diferentes (salt aleatorio)
- **`authenticateWithInvalidPasswordReturnsFalse`**: Verifica que contraseñas incorrectas son rechazadas
- **`loginWithValidCredentialsReturnsSuccess`**: Verifica login exitoso con verificación BCrypt

---

## Despliegue en AWS

### Servidor 1 — Apache (Cliente HTML+JS)

1. **Lanzar instancia EC2** (Amazon Linux 2023)
2. **Instalar Apache:**
   ```bash
   sudo dnf install httpd mod_ssl -y
   sudo systemctl start httpd
   sudo systemctl enable httpd
   ```
3. **Copiar archivos estáticos** (index.html, app.html, css/, js/) a `/var/www/html/`
4. **Configurar dominio** con DuckDNS apuntando a la IP de la instancia
5. **Instalar certificado Let's Encrypt:**
   ```bash
   sudo dnf install certbot python3-certbot-apache -y
   sudo certbot --apache -d arepeci.duckdns.org
   ```
6. **Copiar configuración de Apache:**
   ```bash
   sudo cp arepeci.duckdns.org.conf /etc/httpd/conf.d/
   sudo apachectl configtest
   sudo systemctl restart httpd
   ```

### Servidor 2 — Spring Boot (Backend REST API)

1. **Lanzar segunda instancia EC2**
2. **Instalar Java 17:**
   ```bash
   sudo dnf install java-17-amazon-corretto -y
   ```
3. **Transferir el JAR:**
   ```bash
   scp -i key.pem target/secure-app-1.0.0.jar ec2-user@<IP>:~/
   ```
4. **Ejecutar la aplicación:**
   ```bash
   java -jar secure-app-1.0.0.jar
   ```
5. **Configurar grupo de seguridad** en AWS para abrir puerto 8443

### Configuración CORS

El archivo `SecurityConfig.java` ya tiene CORS habilitado para permitir requests desde el servidor Apache hacia el servidor Spring Boot.

---

##  Endpoints REST API

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| GET | `/api/hello` | Mensaje de saludo seguro | No |
| GET | `/api/status` | Estado del servidor + hilo actual | No |
| POST | `/api/login` | Autenticación con BCrypt | No (es el endpoint de auth) |
| GET | `/api/secure-read?url=...` | Lee URL HTTPS de forma segura | No |

### Ejemplo de petición de login:

```bash
curl -k -X POST https://localhost:8443/api/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Respuesta exitosa:
```json
{
  "success": true,
  "message": "Login exitoso. ¡Bienvenido, admin!",
  "username": "admin"
}
```

---

##  Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 3.2.4 | Framework web + seguridad |
| Spring Security | 6.1.x | Autenticación y autorización |
| BCrypt | - | Hash de contraseñas |
| Maven | 3.9+ | Gestión de dependencias y build |
| Apache HTTP | 2.4+ | Servidor web para cliente estático |
| Let's Encrypt | - | Certificados TLS en producción |
| keytool | JDK 17 | Generación de keystores PKCS12 |
| HTML5 + JS | ES6+ | Cliente web asíncrono |

---

##  Licencia

Escuela Colombiana de ingenieria Julio Garavito
