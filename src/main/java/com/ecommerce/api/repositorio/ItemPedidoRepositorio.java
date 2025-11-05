package com.ecommerce.api.repositorio;

import com.ecommerce.api.entidade.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemPedidoRepositorio extends JpaRepository<ItemPedido, UUID> {
}
