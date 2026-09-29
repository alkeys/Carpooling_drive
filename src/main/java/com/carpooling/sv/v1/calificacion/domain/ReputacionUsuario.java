package com.carpooling.sv.v1.calificacion.domain;

import java.util.List;
import java.util.UUID;

public record ReputacionUsuario(
    UUID usuarioId,
    double promedioPuntuacion,
    int totalResenas,
    List<Calificacion> ultimasCalificaciones
) {
    public ReputacionUsuario {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        if (ultimasCalificaciones == null) {
            ultimasCalificaciones = List.of();
        }
    }
}
