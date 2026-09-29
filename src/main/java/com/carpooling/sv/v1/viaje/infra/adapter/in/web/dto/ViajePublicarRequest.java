package com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para la publicación de un nuevo viaje por un conductor")
public class ViajePublicarRequest {

    @NotNull(message = "El vehículo es obligatorio")
    @Schema(description = "UUID del vehículo registrado por el conductor", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID vehiculoId;

    @Schema(description = "UUID opcional de una ruta del catálogo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID rutaId;

    @NotBlank(message = "El nombre del lugar de origen es obligatorio")
    @Size(max = 150, message = "El origen no puede exceder 150 caracteres")
    @Schema(description = "Nombre o dirección de partida", example = "Metrocentro Santa Ana")
    private String origenNombre;

    @NotNull(message = "La latitud de origen es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud de origen mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud de origen máxima es 90.0")
    @Schema(description = "Latitud del punto de partida", example = "13.9782")
    private Double origenLat;

    @NotNull(message = "La longitud de origen es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud de origen mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud de origen máxima es 180.0")
    @Schema(description = "Longitud del punto de partida", example = "-89.5638")
    private Double origenLon;

    @NotBlank(message = "El nombre del lugar de destino es obligatorio")
    @Size(max = 150, message = "El destino no puede exceder 150 caracteres")
    @Schema(description = "Nombre o dirección de llegada", example = "Plaza Salvador del Mundo, San Salvador")
    private String destinoNombre;

    @NotNull(message = "La latitud de destino es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud de destino mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud de destino máxima es 90.0")
    @Schema(description = "Latitud del punto de llegada", example = "13.7013")
    private Double destinoLat;

    @NotNull(message = "La longitud de destino es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud de destino mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud de destino máxima es 180.0")
    @Schema(description = "Longitud del punto de llegada", example = "-89.2244")
    private Double destinoLon;

    @NotNull(message = "La fecha de salida es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de salida del viaje (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate fechaSalida;

    @NotNull(message = "La hora de salida es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Hora de salida programada (HH:mm)", example = "06:30")
    private LocalTime horaSalida;

    @NotNull(message = "Los cupos totales son obligatorios")
    @Min(value = 1, message = "Debe ofrecer al menos 1 cupo")
    @Schema(description = "Cantidad de asientos disponibles para pasajeros", example = "3")
    private Integer cuposTotales;

    @DecimalMin(value = "0.0", message = "La tarifa aproximada no puede ser negativa")
    @Schema(description = "Costo sugerido o tarifa por pasajero en USD", example = "3.50")
    private BigDecimal tarifaAproximada;

    @Schema(description = "Comentarios o notas de viaje", example = "Salida puntual. No se permite fumar. Espacio para equipaje pequeño.")
    private String descripcion;

    @Valid
    @Schema(description = "Lista opcional de paradas intermedias a lo largo de la ruta")
    private List<PuntoEncuentroRequest> puntosEncuentro;

    public ViajePublicarRequest(UUID vehiculoId, UUID rutaId, String origenNombre, Double origenLat, Double origenLon,
                                String destinoNombre, Double destinoLat, Double destinoLon, LocalDate fechaSalida,
                                LocalTime horaSalida, Integer cuposTotales, BigDecimal tarifaAproximada,
                                String descripcion, List<PuntoEncuentroRequest> puntosEncuentro) {
        this.vehiculoId = vehiculoId;
        this.rutaId = rutaId;
        this.origenNombre = origenNombre;
        this.origenLat = origenLat;
        this.origenLon = origenLon;
        this.destinoNombre = destinoNombre;
        this.destinoLat = destinoLat;
        this.destinoLon = destinoLon;
        this.fechaSalida = fechaSalida;
        this.horaSalida = horaSalida;
        this.cuposTotales = cuposTotales;
        this.tarifaAproximada = tarifaAproximada;
        this.descripcion = descripcion;
        this.puntosEncuentro = puntosEncuentro;
    }
}
