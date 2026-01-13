package com.myname.finguard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI finguardOpenApi() {
                return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT is stored in httpOnly cookie; for Swagger use Authorization: Bearer <token>")))
                .info(new Info()
                        .title("FinGuard API")
                        .description("Personal finance API: auth, accounts, categories, transactions, reports, FX rates, and crypto rates. "
                                + "Auth uses JWT stored in httpOnly cookies; for API clients you can also pass Authorization: Bearer <token>.")
                        .version("0.0.1")
                        .contact(new Contact().name("FinGuard Team").email("support@finguard.local")));
    }
}
