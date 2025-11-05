package com.ecommerce.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequisicaoCriarPedido {

    @NotEmpty(message = "O pedido deve conter pelo menos um item")
    @Valid
    private List<RequisicaoItemPedido> itens;
}
