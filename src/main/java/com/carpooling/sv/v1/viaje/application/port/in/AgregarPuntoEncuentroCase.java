package com.carpooling.sv.v1.viaje.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.PuntoEncuentro;

public interface AgregarPuntoEncuentroCase {
    PuntoEncuentro agregarPuntoEncuentro(UUID viajeId, PuntoEncuentro punto);
}
