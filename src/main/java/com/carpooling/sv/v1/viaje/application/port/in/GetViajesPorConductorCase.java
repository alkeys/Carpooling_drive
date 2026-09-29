package com.carpooling.sv.v1.viaje.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.Viaje;

public interface GetViajesPorConductorCase {
    List<Viaje> getViajesPorConductor(UUID conductorId);
}
