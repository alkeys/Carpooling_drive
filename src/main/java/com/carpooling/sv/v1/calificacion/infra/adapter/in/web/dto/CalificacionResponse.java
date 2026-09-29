package com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.carpooling.sv.v1.calificacion.domain.Calificacion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una reseña o calificación recibida")
public record CalificacionResponse(
    @Schema(description = "ID único de la calificación (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "ID del viaje (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID viajeId,

    @Schema(description = "ID del usuario que emite la reseña (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID evaluadorId,

    @Schema(description = "ID del usuario evaluado (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID evaluadoId,

    @Schema(description = "Puntuación otorgada (1 a 5)", example = "5")
    int puntuacion,

    @Schema(description = "Comentario de la reseña", example = "Excelente servicio y puntualidad")
    String comentario,

    @Schema(description = "Fecha de emisión de la calificación")
    OffsetDateTime creadoEn
) {
    public static CalificacionResponse fromDomain(Calificacion domain) {
        return new CalificacionResponse(
            domain.id(),
            domain.viajeId(),
            domain.evaluadorId(),
            domain.evaluadoId(),
            domain.puntuacion(),
            domain.comentario(),
            domain.creadoEn()
        );
    }
}
