@echo off
echo ========================================
echo  E-Commerce API - Inicializacao
echo ========================================
echo.

REM Verifica se Docker esta rodando
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Docker nao esta rodando!
    echo Por favor, inicie o Docker Desktop e tente novamente.
    pause
    exit /b 1
)

echo [OK] Docker esta rodando
echo.

REM Verifica se porta 3306 esta disponivel
netstat -ano | findstr :3306 >nul 2>&1
if %errorlevel% equ 0 (
    echo [AVISO] Porta 3306 esta em uso!
    echo Usando arquivo alternativo com porta 3307...
    echo.
    set COMPOSE_FILE=docker-compose-alt.yml
) else (
    echo [OK] Porta 3306 disponivel
    set COMPOSE_FILE=docker-compose.yml
)

echo Usando arquivo: %COMPOSE_FILE%
echo.

REM Pergunta se deve fazer rebuild
echo Deseja fazer rebuild completo? (S/N)
set /p REBUILD=
if /i "%REBUILD%"=="S" (
    echo.
    echo Fazendo rebuild completo...
    docker-compose -f %COMPOSE_FILE% down -v
    docker-compose -f %COMPOSE_FILE% build --no-cache
)

echo.
echo Iniciando containers...
docker-compose -f %COMPOSE_FILE% up -d
echo.
echo ========================================
echo  Containers iniciados com sucesso!
echo ========================================
echo.
echo Aguarde 30-60 segundos para inicializacao completa...
echo.
timeout /t 10 /nobreak >nul

REM Verifica status dos containers
docker-compose -f %COMPOSE_FILE% ps

echo.
echo ========================================
echo  Endpoints disponiveis:
echo ========================================
echo  API:     http://localhost:8080
echo  Swagger: http://localhost:8080/swagger-ui.html
echo  Docs:    http://localhost:8080/api-docs
echo  Adminer: http://localhost:8081
echo.
echo ========================================
echo.
echo Para ver logs em tempo real:
echo   docker-compose -f %COMPOSE_FILE% logs -f
echo.
echo Para parar:
echo   docker-compose -f %COMPOSE_FILE% down
echo.

pause
