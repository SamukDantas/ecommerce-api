package com.ecommerce.api.controlador;

import com.ecommerce.api.dto.RequisicaoProduto;
import com.ecommerce.api.dto.RespostaProduto;
import com.ecommerce.api.servico.ServicoProduto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador de produtos
 * GET é público, outras operações apenas para ADMIN
 */
@Tag(name = "Produtos", description = "Gerenciamento de produtos do e-commerce")
@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ControladorProduto {

    private final ServicoProduto servicoProduto;

    @Operation(summary = "Criar produto", description = "Cria um novo produto (apenas ADMIN)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RespostaProduto> criarProduto(@Valid @RequestBody RequisicaoProduto requisicao) {
        RespostaProduto resposta = servicoProduto.criarProduto(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @Operation(summary = "Listar todos os produtos", description = "Lista todos os produtos disponíveis")
    @GetMapping
    public ResponseEntity<List<RespostaProduto>> listarTodosProdutos() {
        List<RespostaProduto> produtos = servicoProduto.listarTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<RespostaProduto> buscarProdutoPorId(@PathVariable UUID id) {
        RespostaProduto produto = servicoProduto.buscarProdutoPorId(id);
        return ResponseEntity.ok(produto);
    }

    @Operation(summary = "Buscar produtos por categoria", description = "Lista produtos de uma categoria específica")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<RespostaProduto>> buscarProdutosPorCategoria(@PathVariable String categoria) {
        List<RespostaProduto> produtos = servicoProduto.buscarProdutosPorCategoria(categoria);
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Atualizar produto", description = "Atualiza um produto existente (apenas ADMIN)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RespostaProduto> atualizarProduto(
            @PathVariable UUID id,
            @Valid @RequestBody RequisicaoProduto requisicao) {
        RespostaProduto resposta = servicoProduto.atualizarProduto(id, requisicao);
        return ResponseEntity.ok(resposta);
    }

    @Operation(summary = "Deletar produto", description = "Remove um produto do sistema (apenas ADMIN)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable UUID id) {
        servicoProduto.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
