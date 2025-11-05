package com.ecommerce.api.excecao;

public class ExcecaoEstoqueInsuficiente extends RuntimeException {
    public ExcecaoEstoqueInsuficiente(String mensagem) {
        super(mensagem);
    }
}
