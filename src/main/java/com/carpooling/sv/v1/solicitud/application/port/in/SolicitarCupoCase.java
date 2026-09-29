package com.carpooling.sv.v1.solicitud.application.port.in;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

public interface SolicitarCupoCase {
    SolicitudViaje solicitarCupo(SolicitudViaje solicitud);
}
