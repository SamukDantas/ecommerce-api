package com.ecommerce.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

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
     * Verifica se há estoque suficiente
     * @param quantidade quantidade desejada
     * @return true se houver estoque suficiente
     */
    public boolean temEstoqueSuficiente(Integer quantidade) {
        return this.quantidadeEstoque >= quantidade;
    }

    /**
     * Diminui o estoque do produto
     * @param quantidade quantidade a ser removida
     */
    public void diminuirEstoque(Integer quantidade) {
        if (!temEstoqueSuficiente(quantidade)) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + nome);
        }
        this.quantidadeEstoque -= quantidade;
    }
}
