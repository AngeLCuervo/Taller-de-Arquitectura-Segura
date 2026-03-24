/**
 * SecureApp - Cliente JavaScript Asíncrono
 * Usa Fetch API con async/await para comunicación segura con el servidor Spring Boot.
 * 
 * @author Angel Cuervo
 */

// ============================================
// Configuración
// ============================================
const API_BASE = '';  // Mismo origen (HTTPS)

// ============================================
// Funciones de Login
// ============================================

/**
 * Maneja el envío del formulario de login.
 * Envía credenciales al servidor de forma asíncrona usando Fetch API.
 */
async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const messageBox = document.getElementById('messageBox');
    const btnText = document.getElementById('btnText');
    const btnLoading = document.getElementById('btnLoading');

    // Mostrar indicador de carga
    btnText.textContent = 'Autenticando...';
    btnLoading.classList.remove('hidden');

    try {
        // Petición POST asíncrona al endpoint de login
        const response = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        // Mostrar mensaje de resultado
        messageBox.classList.remove('hidden', 'success', 'error');

        if (data.success) {
            messageBox.classList.add('success');
            messageBox.textContent = data.message;

            // Guardar sesión y redirigir
            sessionStorage.setItem('loggedUser', username);
            setTimeout(() => {
                window.location.href = '/app.html';
            }, 1000);
        } else {
            messageBox.classList.add('error');
            messageBox.textContent = data.message;
        }
    } catch (error) {
        messageBox.classList.remove('hidden', 'success', 'error');
        messageBox.classList.add('error');
        messageBox.textContent = 'Error de conexión: ' + error.message;
    } finally {
        // Restaurar botón
        btnText.textContent = 'Iniciar Sesión';
        btnLoading.classList.add('hidden');
    }
}

// ============================================
// Funciones del Panel Principal
// ============================================

/**
 * Invoca el servicio /api/hello de forma asíncrona.
 */
async function callHelloService() {
    const resultBox = document.getElementById('helloResult');
    resultBox.textContent = 'Enviando solicitud...';

    try {
        const response = await fetch(`${API_BASE}/api/hello`);
        const data = await response.json();
        resultBox.textContent = JSON.stringify(data, null, 2);
    } catch (error) {
        resultBox.textContent = 'Error: ' + error.message;
    }
}

/**
 * Invoca el servicio /api/status de forma asíncrona.
 */
async function callStatusService() {
    const resultBox = document.getElementById('statusResult');
    resultBox.textContent = 'Consultando estado...';

    try {
        const response = await fetch(`${API_BASE}/api/status`);
        const data = await response.json();
        resultBox.textContent = JSON.stringify(data, null, 2);
    } catch (error) {
        resultBox.textContent = 'Error: ' + error.message;
    }
}

/**
 * Invoca el servicio /api/secure-read de forma asíncrona.
 * Lee una URL HTTPS usando el SSLContext seguro del servidor.
 */
async function callSecureRead() {
    const urlInput = document.getElementById('urlInput');
    const resultBox = document.getElementById('secureReadResult');
    const urlToRead = urlInput.value.trim() || 'https://www.google.com';

    resultBox.textContent = 'Leyendo URL segura: ' + urlToRead + '...';

    try {
        const response = await fetch(
            `${API_BASE}/api/secure-read?url=${encodeURIComponent(urlToRead)}`
        );
        const data = await response.json();
        resultBox.textContent = JSON.stringify(data, null, 2);
    } catch (error) {
        resultBox.textContent = 'Error: ' + error.message;
    }
}

/**
 * Test de concurrencia: envía múltiples solicitudes simultáneamente.
 * Demuestra el soporte de solicitudes concurrentes del servidor.
 */
async function testConcurrency() {
    const resultBox = document.getElementById('concurrencyResult');
    resultBox.textContent = 'Enviando 5 solicitudes concurrentes...';

    const startTime = performance.now();

    try {
        // Crear 5 promesas simultáneas (solicitudes concurrentes)
        const promises = [];
        for (let i = 1; i <= 5; i++) {
            promises.push(
                fetch(`${API_BASE}/api/status`)
                    .then(res => res.json())
                    .then(data => ({
                        request: i,
                        thread: data.thread,
                        timestamp: data.timestamp,
                        status: 'OK'
                    }))
                    .catch(err => ({
                        request: i,
                        status: 'ERROR',
                        error: err.message
                    }))
            );
        }

        // Esperar todas las respuestas
        const results = await Promise.all(promises);
        const elapsed = (performance.now() - startTime).toFixed(0);

        const output = {
            totalRequests: 5,
            elapsedMs: elapsed,
            results: results
        };

        resultBox.textContent = JSON.stringify(output, null, 2);
    } catch (error) {
        resultBox.textContent = 'Error: ' + error.message;
    }
}

/**
 * Cierra la sesión del usuario y redirige al login.
 */
function logout() {
    sessionStorage.removeItem('loggedUser');
    window.location.href = '/index.html';
}
