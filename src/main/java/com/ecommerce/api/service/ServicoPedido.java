package com.ecommerce.api.service;

import com.ecommerce.api.dto.*;
import com.ecommerce.api.entity.ItemPedido;
import com.ecommerce.api.entity.Pedido;
import com.ecommerce.api.entity.Produto;
import com.ecommerce.api.entity.Usuario;
import com.ecommerce.api.enums.StatusPedido;
import com.ecommerce.api.exception.ExcecaoNegocio;
import com.ecommerce.api.exception.ExcecaoEstoqueInsuficiente;
import com.ecommerce.api.exception.ExcecaoRecursoNaoEncontrado;
import com.ecommerce.api.repository.PedidoRepositorio;
import com.ecommerce.api.repository.ProdutoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de pedidos
 * Implementa todas as regras de negócio relacionadas a pedidos
 * Aplica princípios SOLID e padrões de design
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicoPedido {

    private final PedidoRepositorio pedidoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    /**
     * Cria um novo pedido
     * Regras de negócio:
     * - Pedido inicia com status PENDENTE
     * - Valida estoque disponível
     * - Calcula valor total dinamicamente
     * - Não atualiza estoque (apenas após pagamento)
     * 
     * @param requisicao dados do pedido
     * @param usuario usuário que está criando o pedido
     * @return pedido criado
     */
    @Transactional
    public RespostaPedido criarPedido(RequisicaoCriarPedido requisicao, Usuario usuario) {
        log.info("Criando pedido para usuário: {}", usuario.getEmail());

        // Valida se há itens no pedido
        if (requisicao.getItens() == null || requisicao.getItens().isEmpty()) {
            throw new ExcecaoNegocio("O pedido deve conter pelo menos um item");
        }

        // Cria o pedido
        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .status(StatusPedido.PENDENTE)
                .itens(new ArrayList<>())
                .build();

        // Adiciona os itens ao pedido e valida estoque
        for (RequisicaoItemPedido itemRequisicao : requisicao.getItens()) {
            Produto produto = produtoRepositorio.findById(itemRequisicao.getProdutoId())
                    .orElseThrow(() -> new ExcecaoRecursoNaoEncontrado(
                            "Produto não encontrado com ID: " + itemRequisicao.getProdutoId()));

            // REGRA DE NEGÓCIO: Valida estoque disponível
            if (!produto.temEstoqueSuficiente(itemRequisicao.getQuantidade())) {
                throw new ExcecaoEstoqueInsuficiente(
                        String.format("Estoque insuficiente para o produto '%s'. " +
                                "Disponível: %d, Solicitado: %d",
                                produto.getNome(),
                                produto.getQuantidadeEstoque(),
                                itemRequisicao.getQuantidade())
                );
            }

            // Cria o item do pedido com o preço atual do produto
            ItemPedido itemPedido = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemRequisicao.getQuantidade())
                    .precoUnitario(produto.getPreco()) // Captura o preço atual
                    .build();

            pedido.adicionarItem(itemPedido);
        }

        // REGRA DE NEGÓCIO: Calcula o valor total dinamicamente
        pedido.setValorTotal(pedido.calcularValorTotal());

        // Salva o pedido
        pedido = pedidoRepositorio.save(pedido);
        
        log.info("Pedido criado com sucesso. ID: {}, Total: {}", pedido.getId(), pedido.getValorTotal());

        return mapearParaResposta(pedido);
    }

    /**
     * Processa o pagamento de um pedido
     * Regras de negócio:
     * - Apenas pedidos PENDENTES podem ser pagos
     * - Valida estoque novamente no momento do pagamento
     * - Atualiza o estoque apenas após pagamento confirmado
     * - Se falhar, cancela o pedido automaticamente
     * 
     * @param pedidoId ID do pedido
     * @param usuario usuário que está pagando
     * @return pedido atualizado
     */
    @Transactional
    public RespostaPedido processarPagamento(UUID pedidoId, Usuario usuario) {
        log.info("Processando pagamento do pedido: {} para usuário: {}", pedidoId, usuario.getEmail());

        // Busca o pedido com itens (evita N+1)
        Pedido pedido = pedidoRepositorio.buscarPorIdComItens(pedidoId);
        if (pedido == null) {
            throw new ExcecaoRecursoNaoEncontrado("Pedido não encontrado com ID: " + pedidoId);
        }

        // REGRA DE NEGÓCIO: Verifica se o pedido pertence ao usuário
        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new ExcecaoNegocio("Você não tem permissão para pagar este pedido");
        }

        // REGRA DE NEGÓCIO: Verifica se o pedido está pendente
        if (!pedido.estaPendente()) {
            throw new ExcecaoNegocio(
                    "Apenas pedidos pendentes podem ser pagos. Status atual: " + pedido.getStatus());
        }

        try {
            // REGRA DE NEGÓCIO: Valida estoque novamente e atualiza
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                
                if (!produto.temEstoqueSuficiente(item.getQuantidade())) {
                    // Se não houver estoque, cancela o pedido
                    pedido.cancelar();
                    pedidoRepositorio.save(pedido);
                    
                    throw new ExcecaoEstoqueInsuficiente(
                            String.format("Pedido cancelado: Estoque insuficiente para o produto '%s'. " +
                                    "Disponível: %d, Necessário: %d",
                                    produto.getNome(),
                                    produto.getQuantidadeEstoque(),
                                    item.getQuantidade())
                    );
                }

                // Atualiza o estoque
                produto.diminuirEstoque(item.getQuantidade());
                produtoRepositorio.save(produto);
            }

            // REGRA DE NEGÓCIO: Marca o pedido como pago
            pedido.marcarComoPago();
            pedido = pedidoRepositorio.save(pedido);

            log.info("Pagamento processado com sucesso para o pedido: {}", pedidoId);

            return mapearParaResposta(pedido);

        } catch (Exception e) {
            // Em caso de erro, cancela o pedido
            log.error("Erro ao processar pagamento do pedido: {}. Cancelando pedido.", pedidoId, e);
            pedido.cancelar();
            pedidoRepositorio.save(pedido);
            throw e;
        }
    }

    /**
     * Lista todos os pedidos do usuário autenticado
     * @param usuario usuário autenticado
     * @return lista de pedidos
     */
    @Transactional(readOnly = true)
    public List<RespostaPedido> buscarMeusPedidos(Usuario usuario) {
        log.info("Buscando pedidos do usuário: {}", usuario.getEmail());
        
        List<Pedido> pedidos = pedidoRepositorio.buscarPorUsuarioIdComItens(usuario.getId());
        return pedidos.stream()
                .map(this::mapearParaResposta)
                .collect(Collectors.toList());
    }

    /**
     * Busca um pedido específico por ID
     * @param pedidoId ID do pedido
     * @param usuario usuário autenticado
     * @return pedido encontrado
     */
    @Transactional(readOnly = true)
    public RespostaPedido buscarPedidoPorId(UUID pedidoId, Usuario usuario) {
        Pedido pedido = pedidoRepositorio.buscarPorIdComItens(pedidoId);
        
        if (pedido == null) {
            throw new ExcecaoRecursoNaoEncontrado("Pedido não encontrado com ID: " + pedidoId);
        }

        // Verifica se o pedido pertence ao usuário
        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new ExcecaoNegocio("Você não tem permissão para visualizar este pedido");
        }

        return mapearParaResposta(pedido);
    }

    /**
     * Cancela um pedido (apenas se estiver pendente)
     * @param pedidoId ID do pedido
     * @param usuario usuário autenticado
     * @return pedido cancelado
     */
    @Transactional
    public RespostaPedido cancelarPedido(UUID pedidoId, Usuario usuario) {
        log.info("Cancelando pedido: {} para usuário: {}", pedidoId, usuario.getEmail());

        Pedido pedido = pedidoRepositorio.findById(pedidoId)
                .orElseThrow(() -> new ExcecaoRecursoNaoEncontrado("Pedido não encontrado com ID: " + pedidoId));

        // Verifica se o pedido pertence ao usuário
        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new ExcecaoNegocio("Você não tem permissão para cancelar este pedido");
        }

        // Apenas pedidos pendentes podem ser cancelados
        if (!pedido.estaPendente()) {
            throw new ExcecaoNegocio(
                    "Apenas pedidos pendentes podem ser cancelados. Status atual: " + pedido.getStatus());
        }

        pedido.cancelar();
        pedido = pedidoRepositorio.save(pedido);

        log.info("Pedido cancelado com sucesso: {}", pedidoId);

        return mapearParaResposta(pedido);
    }

    /**
     * Mapeia entidade Pedido para DTO de resposta
     * @param pedido entidade do pedido
     * @return DTO de resposta
     */
    private RespostaPedido mapearParaResposta(Pedido pedido) {
        List<RespostaItemPedido> itensResposta = pedido.getItens().stream()
                .map(this::mapearItemParaResposta)
                .collect(Collectors.toList());

        return RespostaPedido.builder()
                .id(pedido.getId())
                .usuarioId(pedido.getUsuario().getId())
                .nomeUsuario(pedido.getUsuario().getNome())
                .status(pedido.getStatus())
                .valorTotal(pedido.getValorTotal())
                .itens(itensResposta)
                .criadoEm(pedido.getCriadoEm())
                .atualizadoEm(pedido.getAtualizadoEm())
                .pagoEm(pedido.getPagoEm())
                .build();
    }

    /**
     * Mapeia entidade ItemPedido para DTO de resposta
     * @param item entidade do item
     * @return DTO de resposta
     */
    private RespostaItemPedido mapearItemParaResposta(ItemPedido item) {
        return RespostaItemPedido.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .nomeProduto(item.getProduto().getNome())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }
}
