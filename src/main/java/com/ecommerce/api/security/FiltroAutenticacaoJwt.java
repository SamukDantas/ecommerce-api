package com.ecommerce.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para validação do token JWT em cada requisição
 * Implementa padrão Chain of Responsibility
 */
@Component
@RequiredArgsConstructor
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private final ServicoJwt servicoJwt;
    private final UserDetailsService servicoDetalhesUsuario;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest requisicao,
            @NonNull HttpServletResponse resposta,
            @NonNull FilterChain cadeiaFiltros
    ) throws ServletException, IOException {
        
        // Extrai o header de autorização
        final String cabecalhoAutorizacao = requisicao.getHeader("Authorization");
        final String jwt;
        final String emailUsuario;

        // Verifica se o header existe e começa com "Bearer "
        if (cabecalhoAutorizacao == null || !cabecalhoAutorizacao.startsWith("Bearer ")) {
            cadeiaFiltros.doFilter(requisicao, resposta);
            return;
        }

        // Extrai o token
        jwt = cabecalhoAutorizacao.substring(7);
        emailUsuario = servicoJwt.extrairNomeUsuario(jwt);

        // Valida o token e autentica o usuário
        if (emailUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails detalhesUsuario = this.servicoDetalhesUsuario.loadUserByUsername(emailUsuario);
            
            if (servicoJwt.tokenEhValido(jwt, detalhesUsuario)) {
                UsernamePasswordAuthenticationToken tokenAutenticacao = new UsernamePasswordAuthenticationToken(
                        detalhesUsuario,
                        null,
                        detalhesUsuario.getAuthorities()
                );
                tokenAutenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));
                SecurityContextHolder.getContext().setAuthentication(tokenAutenticacao);
            }
        }
        
        cadeiaFiltros.doFilter(requisicao, resposta);
    }
}
