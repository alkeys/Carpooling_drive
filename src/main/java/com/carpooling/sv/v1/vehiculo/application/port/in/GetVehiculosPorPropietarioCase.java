package com.carpooling.sv.v1.vehiculo.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;

public interface GetVehiculosPorPropietarioCase {
    List<Vehiculo> getVehiculosPorPropietario(UUID propietarioId);
}
