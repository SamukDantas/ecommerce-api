package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Pedido;
import com.ecommerce.api.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, UUID> {

    /**
     * Busca pedidos de um usuário
     * Usa FETCH JOIN para evitar N+1 queries
     */
    @Query("""
            SELECT DISTINCT p FROM Pedido p
            LEFT JOIN FETCH p.itens i
            LEFT JOIN FETCH i.produto
            WHERE p.usuario.id = :usuarioId
            ORDER BY p.criadoEm DESC
            """)
    List<Pedido> buscarPorUsuarioIdComItens(@Param("usuarioId") UUID usuarioId);

    /**
     * Busca pedidos por status
     */
    List<Pedido> findByStatus(StatusPedido status);

    /**
     * Valor total faturado no mês atual
     * Query otimizada com SUM e filtragem por data
     */
    @Query("""
            SELECT COALESCE(SUM(p.valorTotal), 0)
            FROM Pedido p
            WHERE p.status = 'PAGO'
            AND YEAR(p.pagoEm) = :ano
            AND MONTH(p.pagoEm) = :mes
            """)
    BigDecimal buscarReceitaTotalPorMes(@Param("ano") int ano, @Param("mes") int mes);

    /**
     * Valor total faturado em um período
     */
    @Query("""
            SELECT COALESCE(SUM(p.valorTotal), 0)
            FROM Pedido p
            WHERE p.status = 'PAGO'
            AND p.pagoEm BETWEEN :dataInicio AND :dataFim
            """)
    BigDecimal buscarReceitaTotalPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Busca pedido com itens (evita N+1)
     */
    @Query("""
            SELECT p FROM Pedido p
            LEFT JOIN FETCH p.itens i
            LEFT JOIN FETCH i.produto
            WHERE p.id = :pedidoId
            """)
    Pedido buscarPorIdComItens(@Param("pedidoId") UUID pedidoId);
}
