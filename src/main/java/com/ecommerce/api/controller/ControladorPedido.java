package com.ecommerce.api.controller;

import com.ecommerce.api.dto.RequisicaoCriarPedido;
import com.ecommerce.api.dto.RespostaPedido;
import com.ecommerce.api.entity.Usuario;
import com.ecommerce.api.service.ServicoPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador de pedidos
 * Todos os endpoints requerem autenticação
 */
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos do usuário")
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ControladorPedido {

    private final ServicoPedido servicoPedido;

    @Operation(summary = "Criar pedido", description = "Cria um novo pedido com status PENDENTE")
    @PostMapping
    public ResponseEntity<RespostaPedido> criarPedido(
            @Valid @RequestBody RequisicaoCriarPedido requisicao,
            @AuthenticationPrincipal Usuario usuario) {
        RespostaPedido resposta = servicoPedido.criarPedido(requisicao, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @Operation(summary = "Listar meus pedidos", description = "Lista todos os pedidos do usuário autenticado")
    @GetMapping("/meus-pedidos")
    public ResponseEntity<List<RespostaPedido>> buscarMeusPedidos(@AuthenticationPrincipal Usuario usuario) {
        List<RespostaPedido> pedidos = servicoPedido.buscarMeusPedidos(usuario);
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico do usuário")
    @GetMapping("/{id}")
    public ResponseEntity<RespostaPedido> buscarPedidoPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuario) {
        RespostaPedido pedido = servicoPedido.buscarPedidoPorId(id, usuario);
        return ResponseEntity.ok(pedido);
    }

    @Operation(
        summary = "Processar pagamento", 
        description = "Processa o pagamento de um pedido PENDENTE e atualiza o estoque"
    )
    @PostMapping("/{id}/pagamento")
    public ResponseEntity<RespostaPedido> processarPagamento(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuario) {
        RespostaPedido resposta = servicoPedido.processarPagamento(id, usuario);
        return ResponseEntity.ok(resposta);
    }

    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido que esteja com status PENDENTE")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<RespostaPedido> cancelarPedido(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuario) {
        RespostaPedido resposta = servicoPedido.cancelarPedido(id, usuario);
        return ResponseEntity.ok(resposta);
    }
}
