package com.ecommerce.api.exception;

public class ExcecaoEstoqueInsuficiente extends RuntimeException {
    public ExcecaoEstoqueInsuficiente(String mensagem) {
        super(mensagem);
    }
}
