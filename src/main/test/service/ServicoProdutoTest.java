package com.ecommerce.api.service;

import com.ecommerce.api.dto.RequisicaoProduto;
import com.ecommerce.api.dto.RespostaProduto;
import com.ecommerce.api.entity.Produto;
import com.ecommerce.api.exception.ExcecaoRecursoNaoEncontrado;
import com.ecommerce.api.repository.ProdutoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ServicoProduto
 * Utiliza JUnit 5, Mockito e AssertJ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Produtos")
class ServicoProdutoTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private ServicoProduto servicoProduto;

    private Produto produtoExemplo;
    private RequisicaoProduto requisicaoProdutoExemplo;
    private UUID produtoId;

    @BeforeEach
    void setUp() {
        produtoId = UUID.randomUUID();
        
        produtoExemplo = Produto.builder()
                .id(produtoId)
                .nome("Notebook Dell")
                .descricao("Notebook Dell Inspiron 15")
                .preco(new BigDecimal("3500.00"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(10)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        requisicaoProdutoExemplo = RequisicaoProduto.builder()
                .nome("Notebook Dell")
                .descricao("Notebook Dell Inspiron 15")
                .preco(new BigDecimal("3500.00"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(10)
                .build();
    }

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void deveCriarProdutoComSucesso() {
        // Given
        when(produtoRepositorio.save(any(Produto.class))).thenReturn(produtoExemplo);

        // When
        RespostaProduto resposta = servicoProduto.criarProduto(requisicaoProdutoExemplo);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getNome()).isEqualTo("Notebook Dell");
        assertThat(resposta.getPreco()).isEqualByComparingTo(new BigDecimal("3500.00"));
        assertThat(resposta.getCategoria()).isEqualTo("Eletrônicos");
        assertThat(resposta.getQuantidadeEstoque()).isEqualTo(10);
        
        verify(produtoRepositorio, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void deveBuscarProdutoPorIdComSucesso() {
        // Given
        when(produtoRepositorio.findById(produtoId)).thenReturn(Optional.of(produtoExemplo));

        // When
        RespostaProduto resposta = servicoProduto.buscarProdutoPorId(produtoId);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(produtoId);
        assertThat(resposta.getNome()).isEqualTo("Notebook Dell");
        
        verify(produtoRepositorio, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto inexistente")
    void deveLancarExcecaoAoBuscarProdutoInexistente() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(produtoRepositorio.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> servicoProduto.buscarProdutoPorId(idInexistente))
                .isInstanceOf(ExcecaoRecursoNaoEncontrado.class)
                .hasMessageContaining("Produto não encontrado com ID:");
        
        verify(produtoRepositorio, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void deveListarTodosProdutos() {
        // Given
        Produto produto2 = Produto.builder()
                .id(UUID.randomUUID())
                .nome("Mouse Logitech")
                .descricao("Mouse sem fio")
                .preco(new BigDecimal("150.00"))
                .categoria("Periféricos")
                .quantidadeEstoque(50)
                .build();

        List<Produto> produtos = Arrays.asList(produtoExemplo, produto2);
        when(produtoRepositorio.findAll()).thenReturn(produtos);

        // When
        List<RespostaProduto> resposta = servicoProduto.listarTodosProdutos();

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta).hasSize(2);
        assertThat(resposta.get(0).getNome()).isEqualTo("Notebook Dell");
        assertThat(resposta.get(1).getNome()).isEqualTo("Mouse Logitech");
        
        verify(produtoRepositorio, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() {
        // Given
        String categoria = "Eletrônicos";
        List<Produto> produtos = Arrays.asList(produtoExemplo);
        when(produtoRepositorio.findByCategoria(categoria)).thenReturn(produtos);

        // When
        List<RespostaProduto> resposta = servicoProduto.buscarProdutosPorCategoria(categoria);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).getCategoria()).isEqualTo(categoria);
        
        verify(produtoRepositorio, times(1)).findByCategoria(categoria);
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void deveAtualizarProdutoComSucesso() {
        // Given
        when(produtoRepositorio.findById(produtoId)).thenReturn(Optional.of(produtoExemplo));
        
        RequisicaoProduto requisicaoAtualizacao = RequisicaoProduto.builder()
                .nome("Notebook Dell Atualizado")
                .descricao("Descrição atualizada")
                .preco(new BigDecimal("3800.00"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(15)
                .build();

        Produto produtoAtualizado = Produto.builder()
                .id(produtoId)
                .nome("Notebook Dell Atualizado")
                .descricao("Descrição atualizada")
                .preco(new BigDecimal("3800.00"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(15)
                .build();

        when(produtoRepositorio.save(any(Produto.class))).thenReturn(produtoAtualizado);

        // When
        RespostaProduto resposta = servicoProduto.atualizarProduto(produtoId, requisicaoAtualizacao);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getNome()).isEqualTo("Notebook Dell Atualizado");
        assertThat(resposta.getPreco()).isEqualByComparingTo(new BigDecimal("3800.00"));
        assertThat(resposta.getQuantidadeEstoque()).isEqualTo(15);
        
        verify(produtoRepositorio, times(1)).findById(produtoId);
        verify(produtoRepositorio, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve deletar produto com sucesso")
    void deveDeletarProdutoComSucesso() {
        // Given
        when(produtoRepositorio.findById(produtoId)).thenReturn(Optional.of(produtoExemplo));
        doNothing().when(produtoRepositorio).delete(produtoExemplo);

        // When
        servicoProduto.deletarProduto(produtoId);

        // Then
        verify(produtoRepositorio, times(1)).findById(produtoId);
        verify(produtoRepositorio, times(1)).delete(produtoExemplo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto inexistente")
    void deveLancarExcecaoAoDeletarProdutoInexistente() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(produtoRepositorio.findById(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> servicoProduto.deletarProduto(idInexistente))
                .isInstanceOf(ExcecaoRecursoNaoEncontrado.class)
                .hasMessageContaining("Produto não encontrado com ID:");
        
        verify(produtoRepositorio, times(1)).findById(idInexistente);
        verify(produtoRepositorio, never()).delete(any(Produto.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver produtos")
    void deveRetornarListaVaziaQuandoNaoHouverProdutos() {
        // Given
        when(produtoRepositorio.findAll()).thenReturn(Arrays.asList());

        // When
        List<RespostaProduto> resposta = servicoProduto.listarTodosProdutos();

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta).isEmpty();
        
        verify(produtoRepositorio, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver produtos na categoria")
    void deveRetornarListaVaziaQuandoNaoHouverProdutosNaCategoria() {
        // Given
        String categoria = "Categoria Inexistente";
        when(produtoRepositorio.findByCategoria(categoria)).thenReturn(Arrays.asList());

        // When
        List<RespostaProduto> resposta = servicoProduto.buscarProdutosPorCategoria(categoria);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta).isEmpty();
        
        verify(produtoRepositorio, times(1)).findByCategoria(categoria);
    }
}
