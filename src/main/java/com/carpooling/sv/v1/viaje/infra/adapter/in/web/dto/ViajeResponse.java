package com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle completo de un viaje de carpooling")
public record ViajeResponse(
    @Schema(description = "ID único del viaje (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "ID del conductor que publica el viaje", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String conductorId,

    @Schema(description = "ID del vehículo asignado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String vehiculoId,

    @Schema(description = "ID de la ruta general asociada (opcional)")
    String rutaId,

    @Schema(description = "Nombre o descripción del origen", example = "Metrocentro Santa Ana")
    String origenNombre,

    @Schema(description = "Latitud de origen", example = "13.9782")
    double origenLat,

    @Schema(description = "Longitud de origen", example = "-89.5638")
    double origenLon,

    @Schema(description = "Nombre o descripción del destino", example = "Plaza Salvador del Mundo, San Salvador")
    String destinoNombre,

    @Schema(description = "Latitud de destino", example = "13.7013")
    double destinoLat,

    @Schema(description = "Longitud de destino", example = "-89.2244")
    double destinoLon,

    @Schema(description = "Fecha de salida (YYYY-MM-DD)", example = "2026-10-01")
    String fechaSalida,

    @Schema(description = "Hora de salida (HH:mm:ss)", example = "06:30:00")
    String horaSalida,

    @Schema(description = "Cupos totales del viaje", example = "3")
    int cuposTotales,

    @Schema(description = "Cupos disponibles en este momento", example = "2")
    int cuposDisponibles,

    @Schema(description = "Tarifa estimada por pasajero (USD)", example = "3.50")
    BigDecimal tarifaAproximada,

    @Schema(description = "Descripción o detalles del viaje", example = "Salida puntual. No fumar.")
    String descripcion,

    @Schema(description = "Estado actual del viaje (PUBLICADO, COMPLETO, EN_CURSO, FINALIZADO, CANCELADO)", example = "PUBLICADO")
    String estado,

    @Schema(description = "Paradas o puntos de encuentro intermedios")
    List<PuntoEncuentroResponse> puntosEncuentro,

    @Schema(description = "Fecha de publicación del viaje")
    String creadoEn,

    @Schema(description = "Fecha de última actualización")
    String actualizadoEn
) {}
