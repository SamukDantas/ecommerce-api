package com.ecommerce.api.seguranca;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço para gerenciamento de tokens JWT
 * Implementa padrão Singleton através do Spring
 */
@Service
public class ServicoJwt {

    @Value("${jwt.secret}")
    private String chaveSecreta;

    @Value("${jwt.expiration}")
    private long expiracao;

    /**
     * Extrai o username (email) do token
     */
    public String extrairNomeUsuario(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma claim específica do token
     */
    public <T> T extrairClaim(String token, Function<Claims, T> resolvedorClaims) {
        final Claims claims = extrairTodasClaims(token);
        return resolvedorClaims.apply(claims);
    }

    /**
     * Gera um token JWT para o usuário
     */
    public String gerarToken(UserDetails detalhesUsuario) {
        return gerarToken(new HashMap<>(), detalhesUsuario);
    }

    /**
     * Gera um token JWT com claims extras
     */
    public String gerarToken(Map<String, Object> claimsExtras, UserDetails detalhesUsuario) {
        return construirToken(claimsExtras, detalhesUsuario, expiracao);
    }

    /**
     * Constrói o token JWT
     */
    private String construirToken(
            Map<String, Object> claimsExtras,
            UserDetails detalhesUsuario,
            long expiracao
    ) {
        return Jwts
                .builder()
                .claims(claimsExtras)
                .subject(detalhesUsuario.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiracao))
                .signWith(obterChaveAssinatura())
                .compact();
    }

    /**
     * Valida se o token é válido para o usuário
     */
    public boolean tokenEhValido(String token, UserDetails detalhesUsuario) {
        final String nomeUsuario = extrairNomeUsuario(token);
        return (nomeUsuario.equals(detalhesUsuario.getUsername())) && !tokenEstaExpirado(token);
    }

    /**
     * Verifica se o token está expirado
     */
    private boolean tokenEstaExpirado(String token) {
        return extrairExpiracao(token).before(new Date());
    }

    /**
     * Extrai a data de expiração do token
     */
    private Date extrairExpiracao(String token) {
        return extrairClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai todas as claims do token
     */
    private Claims extrairTodasClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(obterChaveAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtém a chave de assinatura
     */
    private SecretKey obterChaveAssinatura() {
        byte[] bytesChave = Decoders.BASE64.decode(chaveSecreta);
        return Keys.hmacShaKeyFor(bytesChave);
    }
}
