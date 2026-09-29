package com.carpooling.sv.v1.solicitud.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

public interface GetSolicitudesPorViajeCase {
    List<SolicitudViaje> getSolicitudesPorViaje(UUID viajeId);
}
