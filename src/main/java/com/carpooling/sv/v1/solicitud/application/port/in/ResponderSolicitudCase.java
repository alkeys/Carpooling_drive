package com.carpooling.sv.v1.solicitud.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

public interface ResponderSolicitudCase {
    SolicitudViaje responderSolicitud(UUID solicitudId, boolean aceptar);
}
