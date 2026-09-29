package com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para modificar un viaje antes de su inicio")
public class ViajeUpdateRequest {

    @Size(max = 150, message = "El origen no puede exceder 150 caracteres")
    @Schema(description = "Nombre o dirección de partida", example = "Metrocentro Santa Ana")
    private String origenNombre;

    @DecimalMin(value = "-90.0", message = "La latitud de origen mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud de origen máxima es 90.0")
    @Schema(description = "Latitud del punto de partida", example = "13.9782")
    private Double origenLat;

    @DecimalMin(value = "-180.0", message = "La longitud de origen mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud de origen máxima es 180.0")
    @Schema(description = "Longitud del punto de partida", example = "-89.5638")
    private Double origenLon;

    @Size(max = 150, message = "El destino no puede exceder 150 caracteres")
    @Schema(description = "Nombre o dirección de llegada", example = "Plaza Salvador del Mundo, San Salvador")
    private String destinoNombre;

    @DecimalMin(value = "-90.0", message = "La latitud de destino mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud de destino máxima es 90.0")
    @Schema(description = "Latitud del punto de llegada", example = "13.7013")
    private Double destinoLat;

    @DecimalMin(value = "-180.0", message = "La longitud de destino mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud de destino máxima es 180.0")
    @Schema(description = "Longitud del punto de llegada", example = "-89.2244")
    private Double destinoLon;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de salida del viaje (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate fechaSalida;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Hora de salida programada (HH:mm)", example = "07:00")
    private LocalTime horaSalida;

    @DecimalMin(value = "0.0", message = "La tarifa aproximada no puede ser negativa")
    @Schema(description = "Costo sugerido o tarifa por pasajero en USD", example = "3.50")
    private BigDecimal tarifaAproximada;

    @Schema(description = "Comentarios o notas de viaje")
    private String descripcion;
}
