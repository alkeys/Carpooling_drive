package com.carpooling.sv.v1.viaje.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.value.Coordenada;

public record PuntoEncuentro(
    UUID id,
    UUID viajeId,
    String nombre,
    String descripcion,
    Coordenada ubicacion,
    int orden,
    boolean permiteSubida,
    boolean permiteBajada,
    OffsetDateTime creadoEn
) {
    public PuntoEncuentro {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del punto de encuentro no puede ser nulo o vacío");
        }
        if (ubicacion == null) {
            throw new IllegalArgumentException("La ubicación geográfica del punto de encuentro es obligatoria");
        }
        if (orden <= 0) {
            throw new IllegalArgumentException("El orden del punto de encuentro debe ser mayor a 0");
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public PuntoEncuentro withId(UUID nuevoId) {
        return new PuntoEncuentro(nuevoId, this.viajeId, this.nombre, this.descripcion, this.ubicacion, this.orden, this.permiteSubida, this.permiteBajada, this.creadoEn);
    }

    public PuntoEncuentro withViajeId(UUID nuevoViajeId) {
        return new PuntoEncuentro(this.id, nuevoViajeId, this.nombre, this.descripcion, this.ubicacion, this.orden, this.permiteSubida, this.permiteBajada, this.creadoEn);
    }
}
