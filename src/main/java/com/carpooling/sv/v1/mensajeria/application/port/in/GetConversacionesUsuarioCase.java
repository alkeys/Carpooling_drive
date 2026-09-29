package com.carpooling.sv.v1.mensajeria.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Conversacion;

public interface GetConversacionesUsuarioCase {
    List<Conversacion> getConversacionesPorUsuario(UUID usuarioId);
}
