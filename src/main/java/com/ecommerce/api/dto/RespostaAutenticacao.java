package com.ecommerce.api.dto;

import com.ecommerce.api.enums.Papel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespostaAutenticacao {
    private String token;
    private String tipo = "Bearer";
    private UUID usuarioId;
    private String nome;
    private String email;
    private Papel papel;
}
