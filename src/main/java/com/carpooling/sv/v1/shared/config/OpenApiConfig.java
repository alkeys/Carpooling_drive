package com.carpooling.sv.v1.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String COOKIE_AUTH_SCHEME = "cookieAuth";
    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Rutea Carpooling API by Alkeys")
                .version("1.0.0")
                .description("API REST para la plataforma de Carpooling Rutea El Salvador. "
                    + "Diseñada con Arquitectura Hexagonal y Vertical Slice Architecture. "
                    + "Programador: Alexander Aviles" 
                    + "Soporta autenticación mediante JWT almacenado en Cookies HttpOnly ('access_token') o mediante Bearer Token.")
                .contact(new Contact()
                    .name("Alexander Aviles")
                    .email("avilesalex1425@gmail.com")
                    .url("https://github.com/alkeys"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .addSecurityItem(new SecurityRequirement()
                .addList(COOKIE_AUTH_SCHEME)
                .addList(BEARER_AUTH_SCHEME))
            .components(new Components()
                .addSecuritySchemes(COOKIE_AUTH_SCHEME, new SecurityScheme()
                    .name("access_token")
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)
                    .description("Cookie HttpOnly 'access_token' enviada automáticamente por el navegador tras iniciar sesión"))
                .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                    .name(BEARER_AUTH_SCHEME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token JWT enviado en el header Authorization: Bearer <token> (útil para clientes móviles, Postman o Swagger)")));
    }
}
