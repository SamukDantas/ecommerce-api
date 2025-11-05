package com.ecommerce.api.entidade;

import com.ecommerce.api.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "pago_em")
    private LocalDateTime pagoEm;

    @PrePersist
    protected void aoCrear() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    /**
     * Adiciona um item ao pedido
     * @param item item a ser adicionado
     */
    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    /**
     * Calcula o valor total do pedido baseado nos preços atuais dos produtos
     * @return valor total
     */
    public BigDecimal calcularValorTotal() {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Marca o pedido como pago
     */
    public void marcarComoPago() {
        this.status = StatusPedido.PAGO;
        this.pagoEm = LocalDateTime.now();
    }

    /**
     * Cancela o pedido
     */
    public void cancelar() {
        this.status = StatusPedido.CANCELADO;
    }

    /**
     * Verifica se o pedido está pendente
     * @return true se estiver pendente
     */
    public boolean estaPendente() {
        return this.status == StatusPedido.PENDENTE;
    }
}
