package com.ecommerce.api.controller;

import com.ecommerce.api.service.ServicoRelatorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador de relatórios
 * Endpoints com queries otimizadas para análise de dados
 * Acesso restrito apenas para ADMIN
 */
@Tag(name = "Relatórios", description = "Relatórios e análises de vendas (apenas ADMIN)")
@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class ControladorRelatorio {

    private final ServicoRelatorio servicoRelatorio;

    @Operation(
        summary = "Top 5 compradores", 
        description = "Retorna os 5 usuários que mais compraram (baseado no número de pedidos)"
    )
    @GetMapping("/top-compradores")
    public ResponseEntity<List<Map<String, Object>>> buscarTop5Compradores() {
        List<Map<String, Object>> relatorio = servicoRelatorio.buscarTop5Compradores();
        return ResponseEntity.ok(relatorio);
    }

    @Operation(
        summary = "Ticket médio por usuário", 
        description = "Retorna o ticket médio de cada usuário"
    )
    @GetMapping("/ticket-medio")
    public ResponseEntity<List<Map<String, Object>>> buscarTicketMedioPorUsuario() {
        List<Map<String, Object>> relatorio = servicoRelatorio.buscarTicketMedioPorUsuario();
        return ResponseEntity.ok(relatorio);
    }

    @Operation(
        summary = "Faturamento do mês atual", 
        description = "Retorna o valor total faturado no mês atual"
    )
    @GetMapping("/receita/mes-atual")
    public ResponseEntity<Map<String, Object>> buscarReceitaMesAtual() {
        BigDecimal receita = servicoRelatorio.buscarReceitaMesAtual();
        return ResponseEntity.ok(Map.of(
            "periodo", "Mês atual",
            "receitaTotal", receita
        ));
    }

    @Operation(
        summary = "Faturamento por mês", 
        description = "Retorna o valor total faturado em um mês específico"
    )
    @GetMapping("/receita/mes")
    public ResponseEntity<Map<String, Object>> buscarReceitaPorMes(
            @RequestParam int ano,
            @RequestParam int mes) {
        BigDecimal receita = servicoRelatorio.buscarReceitaPorMes(ano, mes);
        return ResponseEntity.ok(Map.of(
            "ano", ano,
            "mes", mes,
            "receitaTotal", receita
        ));
    }

    @Operation(
        summary = "Faturamento por período", 
        description = "Retorna o valor total faturado em um período específico"
    )
    @GetMapping("/receita/periodo")
    public ResponseEntity<Map<String, Object>> buscarReceitaPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        BigDecimal receita = servicoRelatorio.buscarReceitaPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(Map.of(
            "dataInicio", dataInicio,
            "dataFim", dataFim,
            "receitaTotal", receita
        ));
    }
}
