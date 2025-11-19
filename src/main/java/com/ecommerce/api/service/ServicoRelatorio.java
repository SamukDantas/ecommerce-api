package com.ecommerce.api.service;

import com.ecommerce.api.repository.PedidoRepositorio;
import com.ecommerce.api.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Serviço de relatórios
 * Implementa queries otimizadas para análise de dados
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicoRelatorio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    /**
     * Retorna os top 5 usuários que mais compraram
     * Query otimizada com JOIN, GROUP BY e ORDER BY
     * 
     * @return lista com dados dos usuários
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> buscarTop5Compradores() {
        log.info("Buscando top 5 compradores");
        return usuarioRepositorio.buscarTop5UsuariosPorCompras();
    }

    /**
     * Retorna o ticket médio de cada usuário
     * Query otimizada com AVG e GROUP BY
     * 
     * @return lista com ticket médio por usuário
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> buscarTicketMedioPorUsuario() {
        log.info("Calculando ticket médio por usuário");
        return usuarioRepositorio.buscarTicketMedioPorUsuario();
    }

    /**
     * Retorna o valor total faturado no mês atual
     * Query otimizada com SUM e filtro por data
     * 
     * @return valor total faturado
     */
    @Transactional(readOnly = true)
    public BigDecimal buscarReceitaMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        int ano = mesAtual.getYear();
        int mes = mesAtual.getMonthValue();
        
        log.info("Calculando faturamento do mês {}/{}", mes, ano);
        return pedidoRepositorio.buscarReceitaTotalPorMes(ano, mes);
    }

    /**
     * Retorna o valor total faturado em um mês específico
     * 
     * @param ano ano
     * @param mes mês (1-12)
     * @return valor total faturado
     */
    @Transactional(readOnly = true)
    public BigDecimal buscarReceitaPorMes(int ano, int mes) {
        log.info("Calculando faturamento do mês {}/{}", mes, ano);
        return pedidoRepositorio.buscarReceitaTotalPorMes(ano, mes);
    }

    /**
     * Retorna o valor total faturado em um período
     * 
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return valor total faturado
     */
    @Transactional(readOnly = true)
    public BigDecimal buscarReceitaPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        log.info("Calculando faturamento do período {} a {}", dataInicio, dataFim);
        return pedidoRepositorio.buscarReceitaTotalPorPeriodo(dataInicio, dataFim);
    }
}
