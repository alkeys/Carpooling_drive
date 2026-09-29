package com.carpooling.sv.v1.calificacion.application.port.in;

import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.calificacion.domain.Calificacion;

public interface GetCalificacionesPorUsuarioCase {
    List<Calificacion> getCalificacionesPorUsuario(UUID usuarioId);
}
