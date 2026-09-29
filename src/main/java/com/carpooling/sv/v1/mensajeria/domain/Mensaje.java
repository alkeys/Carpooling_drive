package com.carpooling.sv.v1.mensajeria.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Mensaje(
    UUID id,
    UUID conversacionId,
    UUID remitenteId,
    String contenido,
    OffsetDateTime enviadoEn,
    OffsetDateTime leidoEn
) {
    public Mensaje {
        if (conversacionId == null) {
            throw new IllegalArgumentException("El ID de conversación es obligatorio");
        }
        if (remitenteId == null) {
            throw new IllegalArgumentException("El ID del remitente es obligatorio");
        }
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío");
        }
        if (enviadoEn == null) {
            enviadoEn = OffsetDateTime.now();
        }
    }

    public Mensaje withId(UUID nuevoId) {
        return new Mensaje(nuevoId, this.conversacionId, this.remitenteId, this.contenido, this.enviadoEn, this.leidoEn);
    }

    public Mensaje withLeido() {
        return new Mensaje(this.id, this.conversacionId, this.remitenteId, this.contenido, this.enviadoEn, OffsetDateTime.now());
    }
}
