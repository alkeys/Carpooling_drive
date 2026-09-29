package com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos requeridos para solicitar uno o más cupos en un viaje")
public record SolicitudCrearRequest(
    @NotNull(message = "El ID del viaje es obligatorio")
    @Schema(description = "ID del viaje al que se desea unir (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID viajeId,

    @Schema(description = "ID del punto de encuentro intermedio para subida (opcional)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID puntoSubidaId,

    @Schema(description = "ID del punto de encuentro intermedio para bajada (opcional)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID puntoBajadaId,

    @Min(value = 1, message = "Debe solicitar al menos 1 cupo")
    @Max(value = 8, message = "No puede solicitar más de 8 cupos en una sola solicitud")
    @Schema(description = "Cantidad de cupos solicitados", example = "1", defaultValue = "1")
    int cantidadCupos,

    @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
    @Schema(description = "Mensaje o nota opcional para el conductor", example = "Hola, viajo con una mochila pequeña.")
    String mensaje
) {}
