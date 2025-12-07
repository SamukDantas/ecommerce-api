package com.ecommerce.api.integration;

import com.ecommerce.api.dto.RequisicaoProduto;
import com.ecommerce.api.entity.Produto;
import com.ecommerce.api.entity.Usuario;
import com.ecommerce.api.enums.Papel;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integração para API de Produtos
 * Utiliza TestContainers com MySQL e REST Assured
 */
@DisplayName("Testes de Integração - API de Produtos")
class ProdutoIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ProdutoRepositorio produtoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ServicoJwt servicoJwt;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Limpa o banco
        produtoRepositorio.deleteAll();
        usuarioRepositorio.deleteAll();

        // Cria usuário admin
        Usuario admin = Usuario.builder()
                .nome("Admin")
                .email("admin@test.com")
                .senha(passwordEncoder.encode("admin123"))
                .papel(Papel.ADMIN)
                .build();
        admin = usuarioRepositorio.save(admin);
        tokenAdmin = servicoJwt.gerarToken(admin);

        // Cria usuário comum
        Usuario usuario = Usuario.builder()
                .nome("Usuario")
                .email("user@test.com")
                .senha(passwordEncoder.encode("user123"))
                .papel(Papel.USUARIO)
                .build();
        usuario = usuarioRepositorio.save(usuario);
        tokenUsuario = servicoJwt.gerarToken(usuario);
    }

    @Test
    @DisplayName("Deve listar produtos sem autenticação")
    void deveListarProdutosSemAutenticacao() {
        // Given
        criarProdutoNoBanco("Notebook", new BigDecimal("3500.00"));

        // When & Then
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/api/produtos")
        .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].nome", equalTo("Notebook"));
    }

    @Test
    @DisplayName("Deve buscar produto por ID sem autenticação")
    void deveBuscarProdutoPorIdSemAutenticacao() {
        // Given
        Produto produto = criarProdutoNoBanco("Mouse", new BigDecimal("150.00"));

        // When & Then
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/api/produtos/" + produto.getId())
        .then()
                .statusCode(200)
                .body("nome", equalTo("Mouse"))
                .body("preco", equalTo(150.00f));
    }

    @Test
    @DisplayName("Deve criar produto com token de admin")
    void deveCriarProdutoComTokenAdmin() {
        // Given
        RequisicaoProduto requisicao = RequisicaoProduto.builder()
                .nome("Teclado Mecânico")
                .descricao("Teclado RGB")
                .preco(new BigDecimal("450.00"))
                .categoria("Periféricos")
                .quantidadeEstoque(20)
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/produtos")
        .then()
                .statusCode(201)
                .body("nome", equalTo("Teclado Mecânico"))
                .body("preco", equalTo(450.00f))
                .body("quantidadeEstoque", equalTo(20));
    }

    @Test
    @DisplayName("Não deve criar produto com token de usuário comum")
    void naoDeveCriarProdutoComTokenUsuario() {
        // Given
        RequisicaoProduto requisicao = RequisicaoProduto.builder()
                .nome("Produto")
                .preco(new BigDecimal("100.00"))
                .categoria("Categoria")
                .quantidadeEstoque(10)
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/produtos")
        .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Não deve criar produto sem autenticação")
    void naoDeveCriarProdutoSemAutenticacao() {
        // Given
        RequisicaoProduto requisicao = RequisicaoProduto.builder()
                .nome("Produto")
                .preco(new BigDecimal("100.00"))
                .categoria("Categoria")
                .quantidadeEstoque(10)
                .build();

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/produtos")
        .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Deve atualizar produto com token de admin")
    void deveAtualizarProdutoComTokenAdmin() {
        // Given
        Produto produto = criarProdutoNoBanco("Produto Original", new BigDecimal("100.00"));

        RequisicaoProduto requisicao = RequisicaoProduto.builder()
                .nome("Produto Atualizado")
                .descricao("Descrição atualizada")
                .preco(new BigDecimal("200.00"))
                .categoria("Nova Categoria")
                .quantidadeEstoque(30)
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .put("/api/produtos/" + produto.getId())
        .then()
                .statusCode(200)
                .body("nome", equalTo("Produto Atualizado"))
                .body("preco", equalTo(200.00f));
    }

    @Test
    @DisplayName("Deve deletar produto com token de admin")
    void deveDeletarProdutoComTokenAdmin() {
        // Given
        Produto produto = criarProdutoNoBanco("Produto para Deletar", new BigDecimal("100.00"));

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
        .when()
                .delete("/api/produtos/" + produto.getId())
        .then()
                .statusCode(204);

        // Verifica se foi deletado
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/api/produtos/" + produto.getId())
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() {
        // Given
        criarProdutoNoBanco("Notebook", new BigDecimal("3500.00"), "Eletrônicos");
        criarProdutoNoBanco("Mouse", new BigDecimal("150.00"), "Periféricos");
        criarProdutoNoBanco("Teclado", new BigDecimal("300.00"), "Periféricos");

        // When & Then
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/api/produtos/categoria/Periféricos")
        .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].categoria", equalTo("Periféricos"))
                .body("[1].categoria", equalTo("Periféricos"));
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao buscar produto inexistente")
    void deveRetornarErro404AoBuscarProdutoInexistente() {
        // When & Then
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/api/produtos/00000000-0000-0000-0000-000000000000")
        .then()
                .statusCode(404)
                .body("erro", equalTo("Not Found"));
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios ao criar produto")
    void deveValidarCamposObrigatoriosAoCriarProduto() {
        // Given
        RequisicaoProduto requisicao = RequisicaoProduto.builder()
                .descricao("Sem nome")
                .build();

        // When & Then
        given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(ContentType.JSON)
                .body(requisicao)
        .when()
                .post("/api/produtos")
        .then()
                .statusCode(400)
                .body("erro", equalTo("Validation Failed"));
    }

    private Produto criarProdutoNoBanco(String nome, BigDecimal preco) {
        return criarProdutoNoBanco(nome, preco, "Categoria Teste");
    }

    private Produto criarProdutoNoBanco(String nome, BigDecimal preco, String categoria) {
        Produto produto = Produto.builder()
                .nome(nome)
                .descricao("Descrição de " + nome)
                .preco(preco)
                .categoria(categoria)
                .quantidadeEstoque(10)
                .build();
        return produtoRepositorio.save(produto);
    }
}
