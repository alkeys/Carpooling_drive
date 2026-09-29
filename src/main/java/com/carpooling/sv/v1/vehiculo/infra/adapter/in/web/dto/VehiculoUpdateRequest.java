package com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para la actualización de un vehículo")
public class VehiculoUpdateRequest {

    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String marca;

    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String modelo;

    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    @Max(value = 2100, message = "El año debe ser menor a 2100")
    @Schema(description = "Año de fabricación", example = "2021")
    private Integer anio;

    @Size(max = 30, message = "El color no puede exceder 30 caracteres")
    @Schema(description = "Color del vehículo", example = "Negro")
    private String color;

    @Schema(description = "Tipo de vehículo", example = "SEDAN")
    private String tipo;

    @Size(max = 20, message = "La placa no puede exceder 20 caracteres")
    @Schema(description = "Número de placa", example = "P-123456")
    private String placa;

    @Min(value = 1, message = "La capacidad mínima es de 1 pasajero")
    @Max(value = 50, message = "La capacidad máxima es de 50 pasajeros")
    @Schema(description = "Capacidad de pasajeros", example = "4")
    private Integer capacidadPasajeros;

    @Schema(description = "URL de fotografía del vehículo")
    private String fotoUrl;
}
