package com.ecommerce.api.dto;

import com.ecommerce.api.enums.StatusPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespostaPedido {
    private UUID id;
    private UUID usuarioId;
    private String nomeUsuario;
    private StatusPedido status;
    private BigDecimal valorTotal;
    private List<RespostaItemPedido> itens;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime pagoEm;
}
