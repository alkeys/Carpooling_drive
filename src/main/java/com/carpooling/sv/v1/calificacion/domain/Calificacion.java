package com.carpooling.sv.v1.calificacion.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Calificacion(
    UUID id,
    UUID viajeId,
    UUID evaluadorId,
    UUID evaluadoId,
    int puntuacion,
    String comentario,
    OffsetDateTime creadoEn
) {
    public Calificacion {
        if (viajeId == null) {
            throw new IllegalArgumentException("El ID del viaje es obligatorio");
        }
        if (evaluadorId == null) {
            throw new IllegalArgumentException("El ID del evaluador es obligatorio");
        }
        if (evaluadoId == null) {
            throw new IllegalArgumentException("El ID del evaluado es obligatorio");
        }
        if (evaluadorId.equals(evaluadoId)) {
            throw new IllegalArgumentException("No puedes calificarte a ti mismo");
        }
        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5 estrellas");
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public Calificacion withId(UUID nuevoId) {
        return new Calificacion(nuevoId, this.viajeId, this.evaluadorId, this.evaluadoId, this.puntuacion, this.comentario, this.creadoEn);
    }
}
