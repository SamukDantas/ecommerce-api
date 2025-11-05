package com.ecommerce.api.excecao;

public class ExcecaoNegocio extends RuntimeException {
    public ExcecaoNegocio(String mensagem) {
        super(mensagem);
    }
}
