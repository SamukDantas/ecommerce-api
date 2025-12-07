package com.ecommerce.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "E-Commerce API",
        version = "1.0.0",
        description = "API REST para gerenciamento de pedidos e produtos de um e-commerce",
        contact = @Contact(
            name = "Samuel Dantas",
            email = "samueldantasbarbosa@hotmail.com"
        )
    )
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    in = SecuritySchemeIn.HEADER
)
public class AplicacaoEcommerceApi {

    // ✅ Adicionar para classes utilitárias (se aplicável)
    private AplicacaoEcommerceApi() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoEcommerceApi.class, args);
    }
}
