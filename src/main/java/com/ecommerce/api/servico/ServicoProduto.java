package com.ecommerce.api.servico;

import com.ecommerce.api.dto.RequisicaoProduto;
import com.ecommerce.api.dto.RespostaProduto;
import com.ecommerce.api.entidade.Produto;
import com.ecommerce.api.excecao.ExcecaoRecursoNaoEncontrado;
import com.ecommerce.api.repositorio.ProdutoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de produtos
 * Implementa todas as operações CRUD
 * Aplica princípios SOLID - Single Responsibility Principle
 */
@Service
@RequiredArgsConstructor
public class ServicoProduto {

    private final ProdutoRepositorio produtoRepositorio;

    /**
     * Cria um novo produto
     * @param requisicao dados do produto
     * @return produto criado
     */
    @Transactional
    public RespostaProduto criarProduto(RequisicaoProduto requisicao) {
        Produto produto = Produto.builder()
                .nome(requisicao.getNome())
                .descricao(requisicao.getDescricao())
                .preco(requisicao.getPreco())
                .categoria(requisicao.getCategoria())
                .quantidadeEstoque(requisicao.getQuantidadeEstoque())
                .build();

        produto = produtoRepositorio.save(produto);
        return mapearParaResposta(produto);
    }

    /**
     * Busca um produto por ID
     * @param id identificador do produto
     * @return produto encontrado
     */
    @Transactional(readOnly = true)
    public RespostaProduto buscarProdutoPorId(UUID id) {
        Produto produto = buscarProdutoPorIdOuLancarExcecao(id);
        return mapearParaResposta(produto);
    }

    /**
     * Lista todos os produtos
     * @return lista de produtos
     */
    @Transactional(readOnly = true)
    public List<RespostaProduto> listarTodosProdutos() {
        return produtoRepositorio.findAll().stream()
                .map(this::mapearParaResposta)
                .collect(Collectors.toList());
    }

    /**
     * Lista produtos por categoria
     * @param categoria categoria desejada
     * @return lista de produtos
     */
    @Transactional(readOnly = true)
    public List<RespostaProduto> buscarProdutosPorCategoria(String categoria) {
        return produtoRepositorio.findByCategoria(categoria).stream()
                .map(this::mapearParaResposta)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um produto existente
     * @param id identificador do produto
     * @param requisicao novos dados do produto
     * @return produto atualizado
     */
    @Transactional
    public RespostaProduto atualizarProduto(UUID id, RequisicaoProduto requisicao) {
        Produto produto = buscarProdutoPorIdOuLancarExcecao(id);

        produto.setNome(requisicao.getNome());
        produto.setDescricao(requisicao.getDescricao());
        produto.setPreco(requisicao.getPreco());
        produto.setCategoria(requisicao.getCategoria());
        produto.setQuantidadeEstoque(requisicao.getQuantidadeEstoque());

        produto = produtoRepositorio.save(produto);
        return mapearParaResposta(produto);
    }

    /**
     * Deleta um produto
     * @param id identificador do produto
     */
    @Transactional
    public void deletarProduto(UUID id) {
        Produto produto = buscarProdutoPorIdOuLancarExcecao(id);
        produtoRepositorio.delete(produto);
    }

    /**
     * Busca produto ou lança exceção se não encontrado
     * @param id identificador do produto
     * @return produto encontrado
     */
    public Produto buscarProdutoPorIdOuLancarExcecao(UUID id) {
        return produtoRepositorio.findById(id)
                .orElseThrow(() -> new ExcecaoRecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    }

    /**
     * Mapeia entidade para DTO de resposta
     * @param produto entidade do produto
     * @return DTO de resposta
     */
    private RespostaProduto mapearParaResposta(Produto produto) {
        return RespostaProduto.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .categoria(produto.getCategoria())
                .quantidadeEstoque(produto.getQuantidadeEstoque())
                .criadoEm(produto.getCriadoEm())
                .atualizadoEm(produto.getAtualizadoEm())
                .build();
    }
}
