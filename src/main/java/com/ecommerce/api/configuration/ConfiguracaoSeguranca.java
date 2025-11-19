package com.ecommerce.api.configuration;

import com.ecommerce.api.security.FiltroAutenticacaoJwt;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança da aplicação
 * Define as regras de autorização e autenticação
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfiguracaoSeguranca {

    private final FiltroAutenticacaoJwt filtroAutenticacaoJwt;
    private final AuthenticationProvider provedorAutenticacao;

    @Bean
    public SecurityFilterChain cadeiaFiltrosSeguranca(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(autorizacao -> autorizacao
                        // Endpoints públicos
                        .requestMatchers(
                                "/api/autenticacao/**",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Endpoints de produtos - GET público, outros apenas ADMIN
                        .requestMatchers("GET", "/api/produtos/**").permitAll()
                        .requestMatchers("/api/produtos/**").hasRole("ADMIN")
                        // Endpoints de pedidos - apenas usuários autenticados
                        .requestMatchers("/api/pedidos/**").authenticated()
                        // Endpoints de relatórios - apenas ADMIN
                        .requestMatchers("/api/relatorios/**").hasRole("ADMIN")
                        // Qualquer outra requisição requer autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessao -> sessao
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(provedorAutenticacao)
                .addFilterBefore(filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
