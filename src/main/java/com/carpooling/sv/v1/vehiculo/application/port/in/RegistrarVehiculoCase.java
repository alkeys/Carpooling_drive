package com.carpooling.sv.v1.vehiculo.application.port.in;

import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;

public interface RegistrarVehiculoCase {
    Vehiculo registrarVehiculo(Vehiculo vehiculo);
}
