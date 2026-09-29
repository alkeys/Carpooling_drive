package com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Datos para registrar una parada o punto de encuentro intermedio")
public class PuntoEncuentroRequest {

    @NotBlank(message = "El nombre del punto de encuentro es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Schema(description = "Nombre o referencia del lugar", example = "Gasolinera Puma El Congo")
    private String nombre;

    @Schema(description = "Descripción o instrucciones adicionales", example = "Frente a la pasarela peatonal")
    private String descripcion;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud máxima es 90.0")
    @Schema(description = "Latitud geográfica en WGS84", example = "13.9056")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud máxima es 180.0")
    @Schema(description = "Longitud geográfica en WGS84", example = "-89.5023")
    private Double longitud;

    @NotNull(message = "El orden es obligatorio")
    @Min(value = 1, message = "El orden debe ser al menos 1")
    @Schema(description = "Secuencia u orden de la parada en el recorrido", example = "1")
    private Integer orden;

    @Schema(description = "Permite subida de pasajeros en este punto", example = "true")
    private Boolean permiteSubida = true;

    @Schema(description = "Permite bajada de pasajeros en este punto", example = "true")
    private Boolean permiteBajada = true;

    public PuntoEncuentroRequest(String nombre, String descripcion, Double latitud, Double longitud, Integer orden, Boolean permiteSubida, Boolean permiteBajada) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.orden = orden;
        this.permiteSubida = permiteSubida != null ? permiteSubida : true;
        this.permiteBajada = permiteBajada != null ? permiteBajada : true;
    }
}
