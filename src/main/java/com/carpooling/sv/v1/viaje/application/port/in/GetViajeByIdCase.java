package com.carpooling.sv.v1.viaje.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.Viaje;

public interface GetViajeByIdCase {
    Viaje getViajeById(UUID id);
}
