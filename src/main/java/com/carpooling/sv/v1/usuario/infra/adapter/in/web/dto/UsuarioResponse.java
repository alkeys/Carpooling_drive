package com.carpooling.sv.v1.usuario.infra.adapter.in.web.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos públicos del usuario devueltos por la API")
public record UsuarioResponse(
    @Schema(description = "Identificador único (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
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

    @Schema(description = "Indica si el usuario ha verificado su cuenta", example = "false")
    boolean verificado,

    @Schema(description = "Estado actual de la cuenta (ACTIVO, SUSPENDIDO, INACTIVO)", example = "ACTIVO")
    String estado,

    @Schema(description = "Roles asignados al usuario", example = "[\"CLIENTE\", \"USUARIO\"]")
    Set<String> roles,

    @Schema(description = "Fecha y hora de registro", example = "2026-09-27T12:00:00Z")
    String creadoEn,

    @Schema(description = "Fecha y hora de última modificación", example = "2026-09-27T12:00:00Z")
    String actualizadoEn
) {
}
