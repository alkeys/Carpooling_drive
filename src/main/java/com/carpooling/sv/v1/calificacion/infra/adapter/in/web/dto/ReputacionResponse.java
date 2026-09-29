package com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.calificacion.domain.ReputacionUsuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen público de la reputación de un conductor o pasajero")
public record ReputacionResponse(
    @Schema(description = "ID del usuario consultado (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID usuarioId,

    @Schema(description = "Promedio de estrellas (1.00 a 5.00)", example = "4.85")
    double promedioPuntuacion,

    @Schema(description = "Cantidad total de reseñas recibidas", example = "24")
    int totalResenas,

    @Schema(description = "Últimas reseñas recibidas")
    List<CalificacionResponse> ultimasCalificaciones
) {
    public static ReputacionResponse fromDomain(ReputacionUsuario domain) {
        return new ReputacionResponse(
            domain.usuarioId(),
            domain.promedioPuntuacion(),
            domain.totalResenas(),
            domain.ultimasCalificaciones().stream()
                .map(CalificacionResponse::fromDomain)
                .toList()
        );
    }
}
