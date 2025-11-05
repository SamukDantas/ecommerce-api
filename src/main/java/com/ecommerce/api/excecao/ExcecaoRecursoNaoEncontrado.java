package com.ecommerce.api.excecao;

public class ExcecaoRecursoNaoEncontrado extends RuntimeException {
    public ExcecaoRecursoNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
