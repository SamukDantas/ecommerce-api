package com.ecommerce.api.exception;

public class ExcecaoNegocio extends RuntimeException {
    public ExcecaoNegocio(String mensagem) {
        super(mensagem);
    }
}
