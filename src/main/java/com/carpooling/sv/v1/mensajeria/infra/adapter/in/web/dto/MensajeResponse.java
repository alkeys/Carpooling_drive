package com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Mensaje;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un mensaje en una conversación")
public record MensajeResponse(
    @Schema(description = "ID único del mensaje (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "ID de la conversación (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID conversacionId,

    @Schema(description = "ID del usuario remitente (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID remitenteId,

    @Schema(description = "Nombre completo del usuario remitente", example = "Rodrigo Morales")
    String remitenteNombre,

    @Schema(description = "Rol del remitente", example = "CONDUCTOR")
    String remitenteRol,

    @Schema(description = "Texto del mensaje", example = "Estoy esperando en la gasolinera.")
    String contenido,

    @Schema(description = "Fecha y hora de envío")
    OffsetDateTime enviadoEn,

    @Schema(description = "Fecha y hora en que fue leído (opcional)")
    OffsetDateTime leidoEn
) {
    public static MensajeResponse fromDomain(Mensaje domain) {
        return fromDomain(domain, "Usuario", "USUARIO");
    }

    public static MensajeResponse fromDomain(Mensaje domain, String remitenteNombre, String remitenteRol) {
        return new MensajeResponse(
            domain.id(),
            domain.conversacionId(),
            domain.remitenteId(),
            remitenteNombre != null ? remitenteNombre : "Usuario",
            remitenteRol != null ? remitenteRol : "USUARIO",
            domain.contenido(),
            domain.enviadoEn(),
            domain.leidoEn()
        );
    }
}
