package com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
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
@Schema(description = "Datos para el registro de un nuevo vehículo de conductor")
public class VehiculoRequest {

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String modelo;

    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    @Max(value = 2100, message = "El año debe ser menor a 2100")
    @Schema(description = "Año de fabricación", example = "2020")
    private Integer anio;

    @Size(max = 30, message = "El color no puede exceder 30 caracteres")
    @Schema(description = "Color del vehículo", example = "Gris Metálico")
    private String color;

    @Schema(description = "Tipo de vehículo (SEDAN, SUV, HATCHBACK, PICKUP, CAMIONETA, VAN, OTRO)", example = "SEDAN")
    private String tipo;

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa no puede exceder 20 caracteres")
    @Schema(description = "Número de placa o matrícula salvadoreña", example = "P-123456")
    private String placa;

    @NotNull(message = "La capacidad de pasajeros es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es de 1 pasajero")
    @Max(value = 50, message = "La capacidad máxima es de 50 pasajeros")
    @Schema(description = "Cantidad máxima de asientos disponibles para pasajeros", example = "4")
    private Integer capacidadPasajeros;

    @Schema(description = "URL de fotografía del vehículo", example = "https://mis-imagenes.com/mi-carro.jpg")
    private String fotoUrl;

    public VehiculoRequest(String marca, String modelo, Integer anio, String color, String tipo, String placa, Integer capacidadPasajeros, String fotoUrl) {
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.tipo = tipo;
        this.placa = placa;
        this.capacidadPasajeros = capacidadPasajeros;
        this.fotoUrl = fotoUrl;
    }
}
