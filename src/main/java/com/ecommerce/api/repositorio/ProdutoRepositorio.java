package com.ecommerce.api.repositorio;

import com.ecommerce.api.entidade.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProdutoRepositorio extends JpaRepository<Produto, UUID> {

    /**
     * Busca produtos por categoria
     * @param categoria categoria do produto
     * @return lista de produtos
     */
    List<Produto> findByCategoria(String categoria);

    /**
     * Busca produtos com estoque disponível
     * @return lista de produtos com estoque > 0
     */
    @Query("SELECT p FROM Produto p WHERE p.quantidadeEstoque > 0")
    List<Produto> buscarProdutosEmEstoque();

    /**
     * Busca produtos com estoque baixo (menor que um limite)
     * @param limite limite de estoque
     * @return lista de produtos
     */
    @Query("SELECT p FROM Produto p WHERE p.quantidadeEstoque < :limite AND p.quantidadeEstoque > 0")
    List<Produto> buscarProdutosComEstoqueBaixo(@Param("limite") Integer limite);
}
