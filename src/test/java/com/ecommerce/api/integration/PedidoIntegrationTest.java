package com.ecommerce.api.integration;

import com.ecommerce.api.dto.RequisicaoCriarPedido;
import com.ecommerce.api.dto.RequisicaoItemPedido;
import com.ecommerce.api.entity.Produto;
import com.ecommerce.api.entity.Usuario;
import com.ecommerce.api.enums.Papel;
import com.ecommerce.api.repository.PedidoRepositorio;
import com.ecommerce.api.repository.ProdutoRepositorio;
import com.ecommerce.api.repository.UsuarioRepositorio;
import com.ecommerce.api.security.ServicoJwt;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Testes de integração para API de Pedidos
 * Testa o fluxo completo de criação e processamento de pedidos
 */
@DisplayName("Testes de Integração - API de Pedidos")
class PedidoIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @Autowired
    private ProdutoRepositorio produtoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ServicoJwt servicoJwt;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenUsuario;
    private Produto produto;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Limpa o banco
        pedidoRepositorio.deleteAll();
        produtoRepositorio.deleteAll();
        usuarioRepositorio.deleteAll();

        // Cria usuário
        Usuario usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@test.com")
                .senha(passwordEncoder.encode("senha123"))
                .papel(Papel.USUARIO)
                .build();
        usuario = usuarioRepositorio.save(usuario);
        tokenUsuario = servicoJwt.gerarToken(usuario);

        // Cria produto
        produto = Produto.builder()
                .nome("Notebook Dell")
                .descricao("Notebook Dell Inspiron 15")
                .preco(new BigDecimal("3500.00"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(10)
                .build();
        produto = produtoRepositorio.save(produto);
    }

    @Test
    @DisplayName("Deve criar pedido com autenticação")
    void deveCriarPedidoComAutenticacao() {
        // Given
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(2)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .statusCode(201)
                .body("status", equalTo("PENDENTE"))
                .body("valorTotal", equalTo(7000.00f))
                .body("itens.size()", equalTo(1))
                .body("itens[0].quantidade", equalTo(2));
    }

    @Test
    @DisplayName("Não deve criar pedido sem autenticação")
    void naoDeveCriarPedidoSemAutenticacao() {
        // Given
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(1)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Não deve criar pedido com estoque insuficiente")
    void naoDeveCriarPedidoComEstoqueInsuficiente() {
        // Given
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(100) // Maior que o estoque disponível
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .statusCode(400)
                .body("erro", equalTo("Insufficient Stock"));
    }

    @Test
    @DisplayName("Deve listar pedidos do usuário")
    void deveListarPedidosDoUsuario() {
        // Given - Cria um pedido primeiro
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(1)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos");

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
        .when()
                .get("/api/pedidos/meus-pedidos")
        .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].status", equalTo("PENDENTE"));
    }

    @Test
    @DisplayName("Deve processar pagamento de pedido pendente")
    void deveProcessarPagamentoDePedidoPendente() {
        // Given - Cria um pedido
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(2)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        String pedidoId = given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .extract()
                .path("id");

        // When & Then - Processa o pagamento
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/pedidos/" + pedidoId + "/pagamento")
        .then()
                .statusCode(200)
                .body("status", equalTo("PAGO"))
                .body("pagoEm", notNullValue());

        // Verifica se o estoque foi atualizado
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/api/produtos/" + produto.getId())
        .then()
                .statusCode(200)
                .body("quantidadeEstoque", equalTo(8)); // 10 - 2
    }

    @Test
    @DisplayName("Deve cancelar pedido pendente")
    void deveCancelarPedidoPendente() {
        // Given - Cria um pedido
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(1)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        String pedidoId = given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .extract()
                .path("id");

        // When & Then - Cancela o pedido
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/pedidos/" + pedidoId + "/cancelar")
        .then()
                .statusCode(200)
                .body("status", equalTo("CANCELADO"));
    }

    @Test
    @DisplayName("Deve buscar pedido específico por ID")
    void deveBuscarPedidoPorId() {
        // Given - Cria um pedido
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(1)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        String pedidoId = given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .extract()
                .path("id");

        // When & Then - Busca o pedido
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
        .when()
                .get("/api/pedidos/" + pedidoId)
        .then()
                .statusCode(200)
                .body("id", equalTo(pedidoId))
                .body("status", equalTo("PENDENTE"));
    }

    @Test
    @DisplayName("Não deve processar pagamento de pedido inexistente")
    void naoDeveProcessarPagamentoDePedidoInexistente() {
        // Given
        UUID pedidoInexistente = UUID.randomUUID();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/pedidos/" + pedidoInexistente + "/pagamento")
        .then()
                .statusCode(404)
                .body("erro", equalTo("Not Found"));
    }

    @Test
    @DisplayName("Deve validar quantidade mínima ao criar pedido")
    void deveValidarQuantidadeMinimaAoCriarPedido() {
        // Given
        RequisicaoItemPedido item = RequisicaoItemPedido.builder()
                .produtoId(produto.getId())
                .quantidade(0) // Quantidade inválida
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(item))
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/pedidos")
        .then()
                .statusCode(400)
                .body("erro", equalTo("Validation Failed"));
    }
}
