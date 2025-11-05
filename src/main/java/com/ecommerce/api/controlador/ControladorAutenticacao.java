package com.ecommerce.api.controlador;

import com.ecommerce.api.dto.RespostaAutenticacao;
import com.ecommerce.api.dto.RequisicaoLogin;
import com.ecommerce.api.dto.RequisicaoRegistro;
import com.ecommerce.api.servico.ServicoAutenticacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticação
 * Endpoints públicos para registro e login
 */
@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
@RestController
@RequestMapping("/api/autenticacao")
@RequiredArgsConstructor
public class ControladorAutenticacao {

    private final ServicoAutenticacao servicoAutenticacao;

    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário no sistema")
    @PostMapping("/registrar")
    public ResponseEntity<RespostaAutenticacao> registrar(@Valid @RequestBody RequisicaoRegistro requisicao) {
        RespostaAutenticacao resposta = servicoAutenticacao.registrar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @Operation(summary = "Login", description = "Autentica um usuário e retorna um token JWT")
    @PostMapping("/login")
    public ResponseEntity<RespostaAutenticacao> login(@Valid @RequestBody RequisicaoLogin requisicao) {
        RespostaAutenticacao resposta = servicoAutenticacao.login(requisicao);
        return ResponseEntity.ok(resposta);
    }
}
