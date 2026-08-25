package br.com.autorepairshop.api

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val bearerJwt = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        return OpenAPI()
            .info(
                Info()
                    .title("Auto Repair Shop")
                    .description("API da oficina")
                    .version("v1"),
            )
            .components(Components().addSecuritySchemes("bearer-jwt", bearerJwt))
            .addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
    }
}
