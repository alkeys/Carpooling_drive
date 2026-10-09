package com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen completo de conversación para auditoría administrativa y mesa de ayuda")
public record ConversacionAdminResponse(
    @Schema(description = "ID único de la conversación")
    UUID id,

    @Schema(description = "ID del viaje asociado")
    UUID viajeId,

    @Schema(description = "Dirección o punto de origen")
    String origen,

    @Schema(description = "Dirección o punto de destino")
    String destino,

    @Schema(description = "ID del conductor del viaje")
    UUID conductorId,

    @Schema(description = "Nombre completo del conductor")
    String conductorNombre,

    @Schema(description = "Correo del conductor")
    String conductorCorreo,

    @Schema(description = "Fecha de inicio de la conversación")
    OffsetDateTime creadoEn,

    @Schema(description = "IDs de participantes en la conversación")
    List<UUID> participantesIds,

    @Schema(description = "Contenido del último mensaje enviado")
    String ultimoMensaje,

    @Schema(description = "Fecha del último mensaje enviado")
    OffsetDateTime ultimoMensajeEn,

    @Schema(description = "Cantidad total de mensajes en la sala")
    long totalMensajes
) {}
