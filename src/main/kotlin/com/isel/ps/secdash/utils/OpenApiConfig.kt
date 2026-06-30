package com.isel.ps.secdash.utils

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Top-level metadata for the generated OpenAPI document (shown in the Swagger UI header
 * and in docs/openapi.yaml). Only loaded under the dev profile, since springdoc itself
 * is disabled in deploy.
 */
@Configuration
@Profile("dev")
class OpenApiConfig {

    @Bean
    fun secdashOpenApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Secdash API")
                .description(
                    "Dashboard de vulnerabilidades de segurança multi-repositório que agrega resultados de análise estática (SAST) e dependency scanning " +
                        "de repositórios alojados no GitHub e no GitLab."
                )
                .version("0.0.1")
                .contact(
                    Contact()
                        .name("Rodrigo Vitorino (nº 49448), Gonçalo Carvalho (nº 49219)")
                        .url("https://github.com/gfdcarvalho/secdash")
                )
        )
        .externalDocs(
            ExternalDocumentation()
                .description("Repositório do projeto")
                .url("https://github.com/gfdcarvalho/secdash")
        )
}