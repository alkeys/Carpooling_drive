package com.carpooling.sv.v1.vehiculo.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;

public interface GetVehiculoByIdCase {
    Vehiculo getVehiculoById(UUID id);
}
