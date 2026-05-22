package com.paylite.wallet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * Provides:
 *   - API metadata (title, description, version, contact)
 *   - JWT Bearer auth scheme so Swagger UI has an "Authorize" button
 *     that lets users paste their token and test protected endpoints
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI payLiteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayLite API")
                        .description("Production-grade digital wallet backend with JWT auth, "
                                + "idempotent P2P transfers, and Redis-based deduplication.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mayank Harnotiya")
                                .url("https://github.com/MayankHarnotiya/payLite")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token from the /api/auth/login response")));
    }
}