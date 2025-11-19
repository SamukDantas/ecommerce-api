package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    /**
     * Top 5 usuários que mais compraram (baseado no número de pedidos pagos)
     * Query otimizada com JOIN e GROUP BY
     */
    @Query("""
            SELECT u.id as usuarioId, u.nome as nomeUsuario, u.email as emailUsuario, 
                   COUNT(p.id) as totalPedidos, SUM(p.valorTotal) as totalGasto
            FROM Usuario u
            JOIN Pedido p ON p.usuario.id = u.id
            WHERE p.status = 'PAGO'
            GROUP BY u.id, u.nome, u.email
            ORDER BY COUNT(p.id) DESC, SUM(p.valorTotal) DESC
            LIMIT 5
            """)
    List<Map<String, Object>> buscarTop5UsuariosPorCompras();

    /**
     * Ticket médio dos pedidos de cada usuário
     * Query otimizada com AVG e GROUP BY
     */
    @Query("""
            SELECT u.id as usuarioId, u.nome as nomeUsuario, u.email as emailUsuario,
                   COUNT(p.id) as totalPedidos, 
                   AVG(p.valorTotal) as ticketMedio,
                   SUM(p.valorTotal) as totalGasto
            FROM Usuario u
            JOIN Pedido p ON p.usuario.id = u.id
            WHERE p.status = 'PAGO'
            GROUP BY u.id, u.nome, u.email
            ORDER BY AVG(p.valorTotal) DESC
            """)
    List<Map<String, Object>> buscarTicketMedioPorUsuario();
}
