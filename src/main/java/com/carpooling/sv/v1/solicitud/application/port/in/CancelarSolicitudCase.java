package com.carpooling.sv.v1.solicitud.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

public interface CancelarSolicitudCase {
    SolicitudViaje cancelarSolicitud(UUID solicitudId);
}
