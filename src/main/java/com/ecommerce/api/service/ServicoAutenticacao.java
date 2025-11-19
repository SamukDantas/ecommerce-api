package com.ecommerce.api.service;

import com.ecommerce.api.dto.RespostaAutenticacao;
import com.ecommerce.api.dto.RequisicaoLogin;
import com.ecommerce.api.dto.RequisicaoRegistro;
import com.ecommerce.api.entity.Usuario;
import com.ecommerce.api.repository.UsuarioRepositorio;
import com.ecommerce.api.security.ServicoJwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de autenticação
 * Gerencia registro e login de usuários
 */
@Service
@RequiredArgsConstructor
public class ServicoAutenticacao {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificadorSenha;
    private final ServicoJwt servicoJwt;
    private final AuthenticationManager gerenciadorAutenticacao;

    /**
     * Registra um novo usuário no sistema
     * @param requisicao dados do usuário
     * @return resposta com token JWT
     */
    @Transactional
    public RespostaAutenticacao registrar(RequisicaoRegistro requisicao) {
        // Verifica se o email já está em uso
        if (usuarioRepositorio.findByEmail(requisicao.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        // Cria o usuário
        Usuario usuario = Usuario.builder()
                .nome(requisicao.getNome())
                .email(requisicao.getEmail())
                .senha(codificadorSenha.encode(requisicao.getSenha()))
                .papel(requisicao.getPapel())
                .build();

        usuarioRepositorio.save(usuario);

        // Gera o token JWT
        String tokenJwt = servicoJwt.gerarToken(usuario);

        return RespostaAutenticacao.builder()
                .token(tokenJwt)
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .papel(usuario.getPapel())
                .build();
    }

    /**
     * Autentica um usuário existente
     * @param requisicao credenciais de login
     * @return resposta com token JWT
     */
    public RespostaAutenticacao login(RequisicaoLogin requisicao) {
        // Autentica o usuário
        gerenciadorAutenticacao.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requisicao.getEmail(),
                        requisicao.getSenha()
                )
        );

        // Busca o usuário
        Usuario usuario = usuarioRepositorio.findByEmail(requisicao.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Gera o token JWT
        String tokenJwt = servicoJwt.gerarToken(usuario);

        return RespostaAutenticacao.builder()
                .token(tokenJwt)
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .papel(usuario.getPapel())
                .build();
    }
}
