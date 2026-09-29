package com.carpooling.sv.v1.ruta.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una ruta del sistema")
public record RutaResponse(
    @Schema(description = "Identificador único de la ruta (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "Nombre de la ruta", example = "Santa Ana -> San Salvador por Panamericana")
    String nombre,

    @Schema(description = "Descripción o detalles de la ruta", example = "Ruta directa por Carretera Panamericana")
    String descripcion,

    @Schema(description = "Estado activo de la ruta en el sistema", example = "true")
    boolean activo,

    @Schema(description = "Fecha de registro")
    String creadoEn,

    @Schema(description = "Fecha de última modificación")
    String actualizadoEn
) {}
