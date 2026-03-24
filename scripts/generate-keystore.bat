@echo off
REM ============================================
REM Script para generar Keystore PKCS12 y TrustStore (Windows)
REM SecureApp - Taller de Arquitectura Segura
REM Autor: Angel Cuervo
REM ============================================

echo === Generando Keystore PKCS12 ===

REM Crear directorio de keystores si no existe
if not exist "src\main\resources" mkdir "src\main\resources"

REM 1. Generar par de llaves y certificado autofirmado
keytool -genkeypair ^
    -alias ecikeypair ^
    -keyalg RSA ^
    -keysize 2048 ^
    -storetype PKCS12 ^
    -keystore src\main\resources\ecikeystore.p12 ^
    -validity 3650 ^
    -storepass 123456 ^
    -dname "CN=localhost, OU=ECI, O=SecureApp, L=Bogota, ST=Cundinamarca, C=CO"

echo.
echo === Keystore generado exitosamente ===
echo Archivo: src\main\resources\ecikeystore.p12
echo.

REM 2. Exportar certificado
echo === Exportando certificado ===
keytool -export ^
    -alias ecikeypair ^
    -keystore src\main\resources\ecikeystore.p12 ^
    -storetype PKCS12 ^
    -storepass 123456 ^
    -file ecicert.cer

echo Certificado exportado: ecicert.cer
echo.

REM 3. Crear TrustStore
echo === Creando TrustStore ===
if not exist "keystores" mkdir "keystores"
keytool -import ^
    -alias ecikeypair ^
    -file ecicert.cer ^
    -keystore keystores\myTrustStore.p12 ^
    -storetype PKCS12 ^
    -storepass 567890 ^
    -noprompt

echo TrustStore creado: keystores\myTrustStore.p12
echo.

REM 4. Verificar keystore
echo === Verificando Keystore ===
keytool -list ^
    -alias ecikeypair ^
    -keystore src\main\resources\ecikeystore.p12 ^
    -storetype PKCS12 ^
    -storepass 123456

echo.
echo === Proceso completado exitosamente ===
echo Ahora puede ejecutar la aplicacion con: mvn spring-boot:run
pause
