package com.carpooling.sv.v1.mensajeria.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Mensaje;

public interface EnviarMensajeCase {
    Mensaje enviarMensaje(UUID conversacionId, String contenido);
}
