package com.ecommerce.api.service;

import com.ecommerce.api.dto.RequisicaoCriarPedido;
import com.ecommerce.api.dto.RequisicaoItemPedido;
import com.ecommerce.api.dto.RespostaPedido;
import com.ecommerce.api.entity.ItemPedido;
import com.ecommerce.api.entity.Pedido;
import com.ecommerce.api.entity.Produto;
import com.ecommerce.api.entity.Usuario;
import com.ecommerce.api.enums.Papel;
import com.ecommerce.api.enums.StatusPedido;
import com.ecommerce.api.exception.ExcecaoEstoqueInsuficiente;
import com.ecommerce.api.exception.ExcecaoNegocio;
import com.ecommerce.api.exception.ExcecaoRecursoNaoEncontrado;
import com.ecommerce.api.repository.PedidoRepositorio;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ServicoPedido
 * Testa todas as regras de negócio relacionadas a pedidos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Pedidos")
class ServicoPedidoTest {

    @Mock
    private PedidoRepositorio pedidoRepositorio;

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private ServicoPedido servicoPedido;

    private Usuario usuario;
    private Produto produto;
    private Pedido pedido;
    private UUID pedidoId;
    private UUID produtoId;

    @BeforeEach
    void setUp() {
        pedidoId = UUID.randomUUID();
        produtoId = UUID.randomUUID();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("João Silva")
                .email("joao@email.com")
                .senha("senha123")
                .papel(Papel.USUARIO)
                .build();

        produto = Produto.builder()
                .id(produtoId)
                .nome("Notebook Dell")
                .descricao("Notebook Dell Inspiron 15")
                .preco(new BigDecimal("3500.00"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(10)
                .build();

        ItemPedido item = ItemPedido.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .quantidade(2)
                .precoUnitario(new BigDecimal("3500.00"))
                .build();

        pedido = Pedido.builder()
                .id(pedidoId)
                .usuario(usuario)
                .status(StatusPedido.PENDENTE)
                .valorTotal(new BigDecimal("7000.00"))
                .itens(new ArrayList<>(Arrays.asList(item)))
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        item.setPedido(pedido);
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso")
    void deveCriarPedidoComSucesso() {
        // Given
        RequisicaoItemPedido itemRequisicao = RequisicaoItemPedido.builder()
                .produtoId(produtoId)
                .quantidade(2)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(itemRequisicao))
                .build();

        when(produtoRepositorio.findById(produtoId)).thenReturn(Optional.of(produto));
        when(pedidoRepositorio.save(any(Pedido.class))).thenReturn(pedido);

        // When
        RespostaPedido resposta = servicoPedido.criarPedido(requisicao, usuario);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(resposta.getValorTotal()).isEqualByComparingTo(new BigDecimal("7000.00"));
        assertThat(resposta.getItens()).hasSize(1);
        
        verify(produtoRepositorio, times(1)).findById(produtoId);
        verify(pedidoRepositorio, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pedido sem itens")
    void deveLancarExcecaoAoCriarPedidoSemItens() {
        // Given
        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(new ArrayList<>())
                .build();

        // When & Then
        assertThatThrownBy(() -> servicoPedido.criarPedido(requisicao, usuario))
                .isInstanceOf(ExcecaoNegocio.class)
                .hasMessageContaining("O pedido deve conter pelo menos um item");
        
        verify(pedidoRepositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pedido com produto inexistente")
    void deveLancarExcecaoAoCriarPedidoComProdutoInexistente() {
        // Given
        UUID produtoInexistente = UUID.randomUUID();
        RequisicaoItemPedido itemRequisicao = RequisicaoItemPedido.builder()
                .produtoId(produtoInexistente)
                .quantidade(1)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(itemRequisicao))
                .build();

        when(produtoRepositorio.findById(produtoInexistente)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> servicoPedido.criarPedido(requisicao, usuario))
                .isInstanceOf(ExcecaoRecursoNaoEncontrado.class)
                .hasMessageContaining("Produto não encontrado com ID:");
        
        verify(produtoRepositorio, times(1)).findById(produtoInexistente);
        verify(pedidoRepositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pedido com estoque insuficiente")
    void deveLancarExcecaoAoCriarPedidoComEstoqueInsuficiente() {
        // Given
        RequisicaoItemPedido itemRequisicao = RequisicaoItemPedido.builder()
                .produtoId(produtoId)
                .quantidade(100) // Maior que o estoque disponível (10)
                .build();

        RequisicaoCriarPedido requisicao = RequisicaoCriarPedido.builder()
                .itens(Arrays.asList(itemRequisicao))
                .build();

        when(produtoRepositorio.findById(produtoId)).thenReturn(Optional.of(produto));

        // When & Then
        assertThatThrownBy(() -> servicoPedido.criarPedido(requisicao, usuario))
                .isInstanceOf(ExcecaoEstoqueInsuficiente.class)
                .hasMessageContaining("Estoque insuficiente");
        
        verify(produtoRepositorio, times(1)).findById(produtoId);
        verify(pedidoRepositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso")
    void deveProcessarPagamentoComSucesso() {
        // Given
        when(pedidoRepositorio.buscarPorIdComItens(pedidoId)).thenReturn(pedido);
        when(produtoRepositorio.save(any(Produto.class))).thenReturn(produto);
        
        Pedido pedidoPago = Pedido.builder()
                .id(pedidoId)
                .usuario(usuario)
                .status(StatusPedido.PAGO)
                .valorTotal(new BigDecimal("7000.00"))
                .itens(pedido.getItens())
                .pagoEm(LocalDateTime.now())
                .build();
        
        when(pedidoRepositorio.save(any(Pedido.class))).thenReturn(pedidoPago);

        // When
        RespostaPedido resposta = servicoPedido.processarPagamento(pedidoId, usuario);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(resposta.getPagoEm()).isNotNull();
        
        verify(pedidoRepositorio, times(1)).buscarPorIdComItens(pedidoId);
        verify(produtoRepositorio, times(1)).save(any(Produto.class));
        verify(pedidoRepositorio, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao processar pagamento de pedido inexistente")
    void deveLancarExcecaoAoProcessarPagamentoDePedidoInexistente() {
        // Given
        when(pedidoRepositorio.buscarPorIdComItens(pedidoId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> servicoPedido.processarPagamento(pedidoId, usuario))
                .isInstanceOf(ExcecaoRecursoNaoEncontrado.class)
                .hasMessageContaining("Pedido não encontrado com ID:");
        
        verify(pedidoRepositorio, times(1)).buscarPorIdComItens(pedidoId);
        verify(pedidoRepositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao processar pagamento de pedido de outro usuário")
    void deveLancarExcecaoAoProcessarPagamentoDePedidoDeOutroUsuario() {
        // Given
        Usuario outroUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Maria")
                .email("maria@email.com")
                .build();

        when(pedidoRepositorio.buscarPorIdComItens(pedidoId)).thenReturn(pedido);

        // When & Then
        assertThatThrownBy(() -> servicoPedido.processarPagamento(pedidoId, outroUsuario))
                .isInstanceOf(ExcecaoNegocio.class)
                .hasMessageContaining("Você não tem permissão para pagar este pedido");
        
        verify(pedidoRepositorio, times(1)).buscarPorIdComItens(pedidoId);
        verify(pedidoRepositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao processar pagamento de pedido não pendente")
    void deveLancarExcecaoAoProcessarPagamentoDePedidoNaoPendente() {
        // Given
        pedido.setStatus(StatusPedido.PAGO);
        when(pedidoRepositorio.buscarPorIdComItens(pedidoId)).thenReturn(pedido);

        // When & Then
        assertThatThrownBy(() -> servicoPedido.processarPagamento(pedidoId, usuario))
                .isInstanceOf(ExcecaoNegocio.class)
                .hasMessageContaining("Apenas pedidos pendentes podem ser pagos");
        
        verify(pedidoRepositorio, times(1)).buscarPorIdComItens(pedidoId);
        verify(pedidoRepositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve cancelar pedido com sucesso")
    void deveCancelarPedidoComSucesso() {
        // Given
        when(pedidoRepositorio.findById(pedidoId)).thenReturn(Optional.of(pedido));
        
        Pedido pedidoCancelado = Pedido.builder()
                .id(pedidoId)
                .usuario(usuario)
                .status(StatusPedido.CANCELADO)
                .valorTotal(new BigDecimal("7000.00"))
                .itens(pedido.getItens())
                .build();
        
        when(pedidoRepositorio.save(any(Pedido.class))).thenReturn(pedidoCancelado);

        // When
        RespostaPedido resposta = servicoPedido.cancelarPedido(pedidoId, usuario);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        
        verify(pedidoRepositorio, times(1)).findById(pedidoId);
        verify(pedidoRepositorio, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve buscar pedidos do usuário")
    void deveBuscarPedidosDoUsuario() {
        // Given
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepositorio.buscarPorUsuarioIdComItens(usuario.getId())).thenReturn(pedidos);

        // When
        List<RespostaPedido> resposta = servicoPedido.buscarMeusPedidos(usuario);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).getUsuarioId()).isEqualTo(usuario.getId());
        
        verify(pedidoRepositorio, times(1)).buscarPorUsuarioIdComItens(usuario.getId());
    }

    @Test
    @DisplayName("Deve buscar pedido por ID")
    void deveBuscarPedidoPorId() {
        // Given
        when(pedidoRepositorio.buscarPorIdComItens(pedidoId)).thenReturn(pedido);

        // When
        RespostaPedido resposta = servicoPedido.buscarPedidoPorId(pedidoId, usuario);

        // Then
        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(pedidoId);
        
        verify(pedidoRepositorio, times(1)).buscarPorIdComItens(pedidoId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar pedido de outro usuário")
    void deveLancarExcecaoAoBuscarPedidoDeOutroUsuario() {
        // Given
        Usuario outroUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Maria")
                .email("maria@email.com")
                .build();

        when(pedidoRepositorio.buscarPorIdComItens(pedidoId)).thenReturn(pedido);

        // When & Then
        assertThatThrownBy(() -> servicoPedido.buscarPedidoPorId(pedidoId, outroUsuario))
                .isInstanceOf(ExcecaoNegocio.class)
                .hasMessageContaining("Você não tem permissão para visualizar este pedido");
        
        verify(pedidoRepositorio, times(1)).buscarPorIdComItens(pedidoId);
    }
}
