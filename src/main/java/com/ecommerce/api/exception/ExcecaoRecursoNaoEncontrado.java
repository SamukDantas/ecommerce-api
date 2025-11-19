package com.ecommerce.api.exception;

public class ExcecaoRecursoNaoEncontrado extends RuntimeException {
    public ExcecaoRecursoNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
