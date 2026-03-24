#!/bin/bash
# ============================================
# Script para generar Keystore PKCS12 y TrustStore
# SecureApp - Taller de Arquitectura Segura
# Autor: Angel Cuervo
# ============================================

echo "=== Generando Keystore PKCS12 ==="
echo "NOTA: Cuando se pida 'first and last name', ingrese 'localhost'"

# Crear directorio de keystores si no existe
mkdir -p src/main/resources

# 1. Generar par de llaves (pública/privada) y certificado autofirmado
keytool -genkeypair \
    -alias ecikeypair \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore src/main/resources/ecikeystore.p12 \
    -validity 3650 \
    -storepass 123456 \
    -dname "CN=localhost, OU=ECI, O=SecureApp, L=Bogota, ST=Cundinamarca, C=CO"

echo ""
echo "=== Keystore generado exitosamente ==="
echo "Archivo: src/main/resources/ecikeystore.p12"
echo ""

# 2. Exportar el certificado del keystore
echo "=== Exportando certificado ==="
keytool -export \
    -alias ecikeypair \
    -keystore src/main/resources/ecikeystore.p12 \
    -storetype PKCS12 \
    -storepass 123456 \
    -file ecicert.cer

echo "Certificado exportado: ecicert.cer"
echo ""

# 3. Crear TrustStore e importar el certificado
echo "=== Creando TrustStore ==="
mkdir -p keystores
keytool -import \
    -alias ecikeypair \
    -file ecicert.cer \
    -keystore keystores/myTrustStore.p12 \
    -storetype PKCS12 \
    -storepass 567890 \
    -noprompt

echo "TrustStore creado: keystores/myTrustStore.p12"
echo ""

# 4. Verificar el keystore
echo "=== Verificando Keystore ==="
keytool -list \
    -alias ecikeypair \
    -keystore src/main/resources/ecikeystore.p12 \
    -storetype PKCS12 \
    -storepass 123456

echo ""
echo "=== Proceso completado exitosamente ==="
echo "Ahora puede ejecutar la aplicación con: mvn spring-boot:run"
