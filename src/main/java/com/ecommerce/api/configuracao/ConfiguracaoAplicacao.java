package com.ecommerce.api.configuracao;

import com.ecommerce.api.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração de beans da aplicação
 * Centraliza a criação de beans compartilhados
 */
@Configuration
@RequiredArgsConstructor
public class ConfiguracaoAplicacao {

    private final UsuarioRepositorio usuarioRepositorio;

    /**
     * Bean para carregar detalhes do usuário
     * Implementa Strategy Pattern para customizar a busca de usuários
     */
    @Bean
    public UserDetailsService servicoDetalhesUsuario() {
        return nomeUsuario -> usuarioRepositorio.findByEmail(nomeUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    /**
     * Bean para autenticação
     * Usa DaoAuthenticationProvider para validar credenciais
     */
    @Bean
    public AuthenticationProvider provedorAutenticacao() {
        DaoAuthenticationProvider provedorAutenticacao = new DaoAuthenticationProvider();
        provedorAutenticacao.setUserDetailsService(servicoDetalhesUsuario());
        provedorAutenticacao.setPasswordEncoder(codificadorSenha());
        return provedorAutenticacao;
    }

    /**
     * Bean para gerenciar autenticação
     */
    @Bean
    public AuthenticationManager gerenciadorAutenticacao(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    /**
     * Bean para criptografia de senhas
     * Usa BCrypt para hash seguro
     */
    @Bean
    public PasswordEncoder codificadorSenha() {
        return new BCryptPasswordEncoder();
    }
}
