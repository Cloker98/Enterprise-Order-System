@echo off
REM ===========================================================
REM Script de Validação de Setup - Enterprise Order System (Windows)
REM ===========================================================

setlocal enabledelayedexpansion

echo ==========================================
echo Validando Setup do Ambiente
echo ==========================================
echo.

set SUCCESS=0
set FAILURES=0

REM 1. Java
echo ==========================================
echo 1. Java JDK
echo ==========================================

where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Java encontrado
    call java -version 2>nul
    set /a SUCCESS+=1
) else (
    echo [ERRO] Java NAO encontrado
    echo   Instale Java 17: https://adoptium.net/
    set /a FAILURES+=1
)

where javac >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Javac encontrado
    set /a SUCCESS+=1
) else (
    echo [ERRO] Javac NAO encontrado - Instale JDK (nao JRE)
    set /a FAILURES+=1
)

if defined JAVA_HOME (
    echo [OK] JAVA_HOME configurado: %JAVA_HOME%
    set /a SUCCESS+=1
) else (
    echo [ERRO] JAVA_HOME NAO configurado
    echo   Configure JAVA_HOME nas variaveis de ambiente
    set /a FAILURES+=1
)
echo.

REM 2. Maven
echo ==========================================
echo 2. Maven
echo ==========================================

where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Maven encontrado
    call mvn -version 2>nul
    set /a SUCCESS+=1
) else (
    echo [ERRO] Maven NAO encontrado
    echo   Instale Maven: https://maven.apache.org/download.cgi
    set /a FAILURES+=1
)
echo.

REM 3. Docker
echo ==========================================
echo 3. Docker
echo ==========================================

where docker >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Docker encontrado
    call docker --version 2>nul
    set /a SUCCESS+=1
) else (
    echo [ERRO] Docker NAO encontrado
    echo   Instale Docker Desktop: https://www.docker.com/products/docker-desktop/
    set /a FAILURES+=1
)

docker ps >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Docker daemon rodando
    set /a SUCCESS+=1
) else (
    echo [ERRO] Docker daemon NAO esta rodando
    echo   Abra Docker Desktop
    set /a FAILURES+=1
)
echo.

REM 4. Git
echo ==========================================
echo 4. Git
echo ==========================================

where git >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Git encontrado
    call git --version 2>nul
    set /a SUCCESS+=1
) else (
    echo [ERRO] Git NAO encontrado
    set /a FAILURES+=1
)

git remote -v | findstr "origin" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Git remote configurado
    git remote -v
    set /a SUCCESS+=1
) else (
    echo [AVISO] Git remote NAO configurado
    echo   Execute: git remote add origin ^<your-repo-url^>
    set /a FAILURES+=1
)
echo.

REM 5. Test Maven Build
echo ==========================================
echo 5. Teste de Build Maven
echo ==========================================

if exist "services\product-service\pom.xml" (
    echo Testando build do product-service...
    cd services\product-service
    mvn clean compile -q >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo [OK] Build com sucesso
        set /a SUCCESS+=1
    ) else (
        echo [ERRO] Build falhou
        echo   Execute: mvn clean compile
        set /a FAILURES+=1
    )
    cd ..\..
) else (
    echo [AVISO] pom.xml nao encontrado
)
echo.

REM 6. Docker Containers
echo ==========================================
echo 6. Containers Docker
echo ==========================================

docker ps | findstr "postgres" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] PostgreSQL rodando
    set /a SUCCESS+=1
) else (
    echo [AVISO] PostgreSQL NAO rodando
    echo   Execute: cd infrastructure\docker ^&^& docker-compose up -d postgres
)

docker ps | findstr "redis" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Redis rodando
    set /a SUCCESS+=1
) else (
    echo [AVISO] Redis NAO rodando
    echo   Execute: cd infrastructure\docker ^&^& docker-compose up -d redis
)

docker ps | findstr "rabbitmq" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] RabbitMQ rodando
    set /a SUCCESS+=1
) else (
    echo [AVISO] RabbitMQ NAO rodando
    echo   Execute: cd infrastructure\docker ^&^& docker-compose up -d rabbitmq
)
echo.

REM Summary
echo ==========================================
echo RESUMO
echo ==========================================
echo Sucessos: %SUCCESS%
echo Falhas: %FAILURES%
echo.

if %FAILURES% EQU 0 (
    echo [OK] TODOS OS CHECKS PASSARAM!
    echo.
    echo Voce esta pronto para desenvolver!
    echo.
    echo Proximos passos:
    echo   1. cd services\product-service
    echo   2. mvn clean install
    echo   3. mvn spring-boot:run
    echo   4. Abra http://localhost:8081/swagger-ui.html
    exit /b 0
) else (
    echo [ERRO] ALGUNS CHECKS FALHARAM
    echo.
    echo Por favor corrija os problemas acima e execute novamente.
    echo Veja SETUP_GUIDE.md para instrucoes detalhadas.
    exit /b 1
)
