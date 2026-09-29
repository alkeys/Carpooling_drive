package com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una parada o punto de encuentro intermedio")
public record PuntoEncuentroResponse(
    @Schema(description = "ID del punto de encuentro", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "ID del viaje asociado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String viajeId,

    @Schema(description = "Nombre del punto de encuentro", example = "Gasolinera Puma El Congo")
    String nombre,

    @Schema(description = "Instrucciones de referencia", example = "Frente a la pasarela peatonal")
    String descripcion,

    @Schema(description = "Latitud geográfica", example = "13.9056")
    double latitud,

    @Schema(description = "Longitud geográfica", example = "-89.5023")
    double longitud,

    @Schema(description = "Orden de la parada", example = "1")
    int orden,

    @Schema(description = "Permite subida", example = "true")
    boolean permiteSubida,

    @Schema(description = "Permite bajada", example = "true")
    boolean permiteBajada
) {}
