package com.carpooling.sv.v1.ruta.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Ruta(
    UUID id,
    String nombre,
    String descripcion,
    boolean activo,
    OffsetDateTime creadoEn,
    OffsetDateTime actualizadoEn
) {
    public Ruta {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la ruta no puede ser nulo o vacío");
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (actualizadoEn == null) {
            actualizadoEn = OffsetDateTime.now();
        }
    }

    public Ruta(String nombre, String descripcion) {
        this(null, nombre, descripcion, true, OffsetDateTime.now(), OffsetDateTime.now());
    }

    public Ruta withId(UUID nuevoId) {
        return new Ruta(nuevoId, this.nombre, this.descripcion, this.activo, this.creadoEn, this.actualizadoEn);
    }

    public Ruta withActivo(boolean nuevoActivo) {
        return new Ruta(this.id, this.nombre, this.descripcion, nuevoActivo, this.creadoEn, OffsetDateTime.now());
    }

    public Ruta withUpdatedData(String nuevoNombre, String nuevaDescripcion) {
        return new Ruta(
            this.id,
            nuevoNombre != null && !nuevoNombre.isBlank() ? nuevoNombre : this.nombre,
            nuevaDescripcion != null ? nuevaDescripcion : this.descripcion,
            this.activo,
            this.creadoEn,
            OffsetDateTime.now()
        );
    }
}
