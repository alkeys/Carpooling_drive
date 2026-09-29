package com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Respuesta del conductor para aceptar o rechazar una solicitud de viaje")
public record ResponderSolicitudRequest(
    @NotNull(message = "El campo 'aceptar' es obligatorio (true para aceptar, false para rechazar)")
    @Schema(description = "true para aceptar la solicitud y reservar cupos; false para rechazarla", example = "true")
    Boolean aceptar
) {}
