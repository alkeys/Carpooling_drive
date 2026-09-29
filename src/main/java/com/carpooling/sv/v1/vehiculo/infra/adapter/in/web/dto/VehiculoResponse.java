package com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta pública con información del vehículo")
public record VehiculoResponse(
    @Schema(description = "Identificador único del vehículo (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "ID del usuario propietario (Conductor)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String propietarioId,

    @Schema(description = "Marca del vehículo", example = "Toyota")
    String marca,

    @Schema(description = "Modelo del vehículo", example = "Corolla")
    String modelo,

    @Schema(description = "Año de fabricación", example = "2020")
    Integer anio,

    @Schema(description = "Color", example = "Gris")
    String color,

    @Schema(description = "Tipo de vehículo", example = "SEDAN")
    String tipo,

    @Schema(description = "Placa del vehículo", example = "P-123456")
    String placa,

    @Schema(description = "Capacidad de pasajeros", example = "4")
    int capacidadPasajeros,

    @Schema(description = "Foto del vehículo")
    String fotoUrl,

    @Schema(description = "Estado activo del vehículo", example = "true")
    boolean activo,

    @Schema(description = "Fecha de registro")
    String creadoEn,

    @Schema(description = "Fecha de última actualización")
    String actualizadoEn
) {}
