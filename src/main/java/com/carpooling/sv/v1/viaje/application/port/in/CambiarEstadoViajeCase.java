package com.carpooling.sv.v1.viaje.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;

public interface CambiarEstadoViajeCase {
    Viaje cambiarEstadoViaje(UUID id, EstadoViaje nuevoEstado);
}
