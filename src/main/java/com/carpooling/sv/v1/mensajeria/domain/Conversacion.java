package com.carpooling.sv.v1.mensajeria.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Conversacion(
    UUID id,
    UUID viajeId,
    OffsetDateTime creadoEn,
    List<UUID> participantesIds
) {
    public Conversacion {
        if (viajeId == null) {
            throw new IllegalArgumentException("El ID del viaje es obligatorio para una conversación");
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (participantesIds == null) {
            participantesIds = List.of();
        }
    }

    public Conversacion withId(UUID nuevoId) {
        return new Conversacion(nuevoId, this.viajeId, this.creadoEn, this.participantesIds);
    }
}
