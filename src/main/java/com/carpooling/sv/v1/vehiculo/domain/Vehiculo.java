package com.carpooling.sv.v1.vehiculo.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo;

public record Vehiculo(
    UUID id,
    UUID propietarioId,
    String marca,
    String modelo,
    Integer anio,
    String color,
    TipoVehiculo tipo,
    String placa,
    int capacidadPasajeros,
    String fotoUrl,
    boolean activo,
    OffsetDateTime creadoEn,
    OffsetDateTime actualizadoEn
) {
    public Vehiculo {
        if (propietarioId == null) {
            throw new IllegalArgumentException("El vehículo debe tener un propietario asignado");
        }
        if (marca == null || marca.isBlank()) {
            throw new IllegalArgumentException("La marca del vehículo no puede ser vacía");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("El modelo del vehículo no puede ser vacío");
        }
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("La placa del vehículo no puede ser vacía");
        }
        if (capacidadPasajeros < 1 || capacidadPasajeros > 50) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe estar entre 1 y 50");
        }
        if (anio != null && (anio < 1900 || anio > 2100)) {
            throw new IllegalArgumentException("El año del vehículo no es válido");
        }
        if (tipo == null) {
            tipo = TipoVehiculo.SEDAN;
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (actualizadoEn == null) {
            actualizadoEn = OffsetDateTime.now();
        }
    }

    public Vehiculo withId(UUID nuevoId) {
        return new Vehiculo(
            nuevoId,
            this.propietarioId,
            this.marca,
            this.modelo,
            this.anio,
            this.color,
            this.tipo,
            this.placa,
            this.capacidadPasajeros,
            this.fotoUrl,
            this.activo,
            this.creadoEn,
            this.actualizadoEn
        );
    }

    public Vehiculo withActivo(boolean nuevoActivo) {
        return new Vehiculo(
            this.id,
            this.propietarioId,
            this.marca,
            this.modelo,
            this.anio,
            this.color,
            this.tipo,
            this.placa,
            this.capacidadPasajeros,
            this.fotoUrl,
            nuevoActivo,
            this.creadoEn,
            OffsetDateTime.now()
        );
    }

    public Vehiculo withUpdatedData(
            String nuevaMarca,
            String nuevoModelo,
            Integer nuevoAnio,
            String nuevoColor,
            TipoVehiculo nuevoTipo,
            String nuevaPlaca,
            Integer nuevaCapacidad,
            String nuevaFotoUrl) {
        return new Vehiculo(
            this.id,
            this.propietarioId,
            nuevaMarca != null && !nuevaMarca.isBlank() ? nuevaMarca : this.marca,
            nuevoModelo != null && !nuevoModelo.isBlank() ? nuevoModelo : this.modelo,
            nuevoAnio != null ? nuevoAnio : this.anio,
            nuevoColor != null ? nuevoColor : this.color,
            nuevoTipo != null ? nuevoTipo : this.tipo,
            nuevaPlaca != null && !nuevaPlaca.isBlank() ? nuevaPlaca : this.placa,
            nuevaCapacidad != null ? nuevaCapacidad : this.capacidadPasajeros,
            nuevaFotoUrl != null ? nuevaFotoUrl : this.fotoUrl,
            this.activo,
            this.creadoEn,
            OffsetDateTime.now()
        );
    }
}
