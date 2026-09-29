package com.carpooling.sv.v1.mensajeria.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Mensaje;

public interface GetMensajesConversacionCase {
    List<Mensaje> getMensajesPorConversacion(UUID conversacionId);
}
