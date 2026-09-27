package com.carpooling.sv.v1.auth.infra.adapter.in.web.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación exitosa con información de sesión")
public record AuthResponse(
    @Schema(description = "Identificador único del usuario (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,

    @Schema(description = "Nombre(s) del usuario", example = "Juan")
    String nombre,

    @Schema(description = "Apellido(s) del usuario", example = "Pérez")
    String apellido,

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@example.com")
    String correo,

    @Schema(description = "Número telefónico del usuario", example = "+50371234567")
    String telefono,

    @Schema(description = "URL de la foto de perfil", example = "https://example.com/foto.jpg")
    String fotourl,

    @Schema(description = "Estado de la cuenta", example = "ACTIVO")
    String estado,

    @Schema(description = "Roles asignados al usuario", example = "[\"CLIENTE\", \"USUARIO\"]")
    Set<String> roles,

    @Schema(description = "Mensaje descriptivo del resultado", example = "Inicio de sesión exitoso. Cookie HttpOnly establecida.")
    String mensaje,

    @Schema(description = "Token JWT generado (también enviado automáticamente en la Cookie HttpOnly)", example = "eyJhbGciOiJIUzI1NiIsIn...")
    String token
) {
}
