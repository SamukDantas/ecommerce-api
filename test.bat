@echo off
echo ========================================
echo  E-Commerce API - Teste Rapido
echo ========================================
echo.

REM Verifica se curl esta disponivel
where curl >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] curl nao encontrado!
    echo Instale curl ou use Postman/Swagger para testar
    pause
    exit /b 1
)

echo Testando conectividade com a API...
echo.

REM Testa endpoint publico
echo 1. Testando GET /api/produtos (publico)...
curl -s http://localhost:8080/api/produtos > nul
if %errorlevel% equ 0 (
    echo [OK] API esta respondendo!
) else (
    echo [ERRO] API nao esta respondendo!
    echo Verifique se os containers estao rodando: docker-compose ps
    pause
    exit /b 1
)

echo.
echo 2. Registrando usuario ADMIN...
curl -s -X POST http://localhost:8080/api/autenticacao/registrar ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"Admin Teste\",\"email\":\"admin@teste.com\",\"senha\":\"admin123\",\"papel\":\"ADMIN\"}" ^
  -o admin_response.json

if %errorlevel% equ 0 (
    echo [OK] Usuario ADMIN registrado
) else (
    echo [AVISO] Erro ao registrar (talvez ja exista)
)

echo.
echo 3. Fazendo login como ADMIN...
curl -s -X POST http://localhost:8080/api/autenticacao/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@teste.com\",\"senha\":\"admin123\"}" ^
  -o login_response.json

REM Extrai token (requer jq ou powershell)
powershell -Command "(Get-Content login_response.json | ConvertFrom-Json).token" > token.txt 2>nul
set /p TOKEN=<token.txt

if defined TOKEN (
    echo [OK] Login realizado com sucesso!
    echo Token: %TOKEN:~0,20%...
) else (
    echo [ERRO] Falha ao fazer login
    type login_response.json
    pause
    exit /b 1
)

echo.
echo 4. Criando produto de teste...
curl -s -X POST http://localhost:8080/api/produtos ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"nome\":\"Produto Teste\",\"descricao\":\"Criado pelo script de teste\",\"preco\":99.90,\"categoria\":\"Teste\",\"quantidadeEstoque\":10}" ^
  -o produto_response.json

if %errorlevel% equ 0 (
    echo [OK] Produto criado com sucesso!
) else (
    echo [ERRO] Falha ao criar produto
)

echo.
echo.
echo 5. Listando todos os produtos...
curl -s http://localhost:8080/api/produtos
echo.

echo.
echo 6. Registrando usuario normal...
curl -s -X POST http://localhost:8080/api/autenticacao/registrar ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"Usuario Teste\",\"email\":\"user@teste.com\",\"senha\":\"user123\",\"papel\":\"USUARIO\"}" ^

if %errorlevel% equ 0 (
    echo [OK] Usuario USER registrado
    type user_response.json
) else (
    echo [AVISO] Erro ao registrar usuario (talvez ja exista)
)

echo.
echo.
echo ========================================
echo  Teste Concluido!
echo ========================================
echo.
echo Credenciais criadas:
echo   ADMIN:   admin@teste.com / admin123
echo   USUARIO: user@teste.com / user123
echo.
echo Endpoints testados:
echo   - POST /api/autenticacao/registrar
echo   - POST /api/autenticacao/login
echo   - POST /api/produtos (ADMIN)
echo   - GET  /api/produtos
echo.
echo Acesse o Swagger para continuar testando:
echo   http://localhost:8080/swagger-ui.html
echo.
echo Arquivos gerados:
echo   - admin_response.json
echo   - login_response.json
echo   - product_response.json
echo   - token.txt
echo.

REM Limpa arquivos temporarios
del token.txt 2>nul

pause
