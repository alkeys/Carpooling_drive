package com.carpooling.sv.v1.shared.exception;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura estándar para respuestas de error de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(description = "Momento en el que ocurrió el error", example = "2026-09-27T18:00:00Z")
    OffsetDateTime timestamp,

    @Schema(description = "Código de estado HTTP", example = "400")
    int status,

    @Schema(description = "Nombre del error HTTP", example = "Bad Request")
    String error,

    @Schema(description = "Mensaje descriptivo del error", example = "El correo ya se encuentra registrado")
    String message,

    @Schema(description = "Ruta del endpoint solicitado", example = "/api/v1/usuarios")
    String path,

    @Schema(description = "Errores de validación específicos por campo (opcional)")
    Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, validationErrors);
    }
}
