package com.carpooling.sv.v1.vehiculo.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;

public interface ActualizarVehiculoCase {
    Vehiculo actualizarVehiculo(
        UUID id,
        String marca,
        String modelo,
        Integer anio,
        String color,
        String tipo,
        String placa,
        Integer capacidadPasajeros,
        String fotoUrl
    );
}
