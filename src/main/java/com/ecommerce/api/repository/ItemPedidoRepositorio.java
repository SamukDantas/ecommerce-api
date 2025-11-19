package com.ecommerce.api.repository;

import com.ecommerce.api.entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemPedidoRepositorio extends JpaRepository<ItemPedido, UUID> {
}
