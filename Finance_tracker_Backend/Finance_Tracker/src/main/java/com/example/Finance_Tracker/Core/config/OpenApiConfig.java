package com.example.Finance_Tracker.Core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for springdoc. The spec is served at /v3/api-docs and Swagger UI at /swagger-ui.html.
 * Every operation requires the JWT bearer token by default; use "Authorize" in Swagger UI with the
 * token returned by POST /api/auth/login.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Finance Tracker API", version = "v1",
                description = "REST API for the Finance Tracker Android app"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
