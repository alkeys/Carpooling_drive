package com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una solicitud de cupo en un viaje")
public record SolicitudResponse(
    @Schema(description = "ID único de la solicitud (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "ID del viaje solicitado (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID viajeId,

    @Schema(description = "ID del pasajero solicitante (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID pasajeroId,

    @Schema(description = "ID del punto de encuentro intermedio para subida (opcional)")
    UUID puntoSubidaId,

    @Schema(description = "ID del punto de encuentro intermedio para bajada (opcional)")
    UUID puntoBajadaId,

    @Schema(description = "Cantidad de cupos solicitados", example = "1")
    int cantidadCupos,

    @Schema(description = "Estado actual de la solicitud (PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA)", example = "PENDIENTE")
    String estado,

    @Schema(description = "Mensaje del pasajero al conductor", example = "Hola, llevo equipaje de mano.")
    String mensaje,

    @Schema(description = "Fecha y hora en que se creó la solicitud")
    OffsetDateTime creadoEn,

    @Schema(description = "Fecha y hora en que el conductor respondió la solicitud")
    OffsetDateTime respondidoEn,

    @Schema(description = "Fecha y hora de última actualización")
    OffsetDateTime actualizadoEn
) {
    public static SolicitudResponse fromDomain(SolicitudViaje domain) {
        return new SolicitudResponse(
            domain.id(),
            domain.viajeId(),
            domain.pasajeroId(),
            domain.puntoSubidaId(),
            domain.puntoBajadaId(),
            domain.cantidadCupos(),
            domain.estado().name(),
            domain.mensaje(),
            domain.creadoEn(),
            domain.respondidoEn(),
            domain.actualizadoEn()
        );
    }
}
