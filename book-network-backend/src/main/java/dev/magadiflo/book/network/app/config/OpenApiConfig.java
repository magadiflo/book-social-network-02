package dev.magadiflo.book.network.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Magadiflo",
                        email = "contact@magadiflo.com",
                        url = "https://magadiflo.com/courses"
                ),
                description = "Documentación OpenAPI para Spring Security",
                title = "OpenAPI Specification - Magadiflo",
                version = "1.0",
                license = @License(
                        name = "Nombre de la licencia",
                        url = "https://some-url.com"
                ),
                termsOfService = "Términos de servicio"
        ),
        servers = {
                @Server(description = "Local ENV", url = "http://localhost:8080"),
                @Server(description = "Prod ENV", url = "https://magadiflo.com")
        },
        security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
