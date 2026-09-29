package com.carpooling.sv.v1.solicitud.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

public interface GetSolicitudesPorPasajeroCase {
    List<SolicitudViaje> getSolicitudesPorPasajero(UUID pasajeroId);
}
