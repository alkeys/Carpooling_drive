package com.carpooling.sv.v1.calificacion.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.calificacion.domain.ReputacionUsuario;

public interface GetReputacionUsuarioCase {
    ReputacionUsuario getReputacionPorUsuario(UUID usuarioId);
}
