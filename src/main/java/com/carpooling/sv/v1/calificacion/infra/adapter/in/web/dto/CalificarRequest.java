package com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para calificar a un conductor o pasajero tras finalizar un viaje")
public record CalificarRequest(
    @NotNull(message = "El ID del viaje es obligatorio")
    @Schema(description = "ID del viaje finalizado (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID viajeId,

    @NotNull(message = "El ID del usuario evaluado es obligatorio")
    @Schema(description = "ID del usuario a calificar (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID evaluadoId,

    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    @Schema(description = "Puntuación de 1 a 5 estrellas", example = "5")
    int puntuacion,

    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    @Schema(description = "Comentario o reseña opcional sobre la experiencia", example = "Excelente conductor, muy puntual y amable.")
    String comentario
) {}
