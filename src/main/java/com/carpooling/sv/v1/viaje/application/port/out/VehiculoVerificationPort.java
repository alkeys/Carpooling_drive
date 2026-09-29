package com.carpooling.sv.v1.viaje.application.port.out;

import java.util.UUID;

public interface VehiculoVerificationPort {
    boolean isVehiculoValidoParaConductor(UUID vehiculoId, UUID conductorId, int cuposRequeridos);
}
