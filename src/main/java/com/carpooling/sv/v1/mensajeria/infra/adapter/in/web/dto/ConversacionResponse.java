package com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Conversacion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una conversación grupal vinculada a un viaje")
public record ConversacionResponse(
    @Schema(description = "ID único de la conversación (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "ID del viaje asociado (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID viajeId,

    @Schema(description = "Fecha de inicio de la conversación")
    OffsetDateTime creadoEn,

    @Schema(description = "Lista de IDs de usuarios participantes en la conversación")
    List<UUID> participantesIds
) {
    public static ConversacionResponse fromDomain(Conversacion domain) {
        return new ConversacionResponse(
            domain.id(),
            domain.viajeId(),
            domain.creadoEn(),
            domain.participantesIds()
        );
    }
}
