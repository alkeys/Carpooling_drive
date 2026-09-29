package com.carpooling.sv.v1.calificacion.application.port.in;

import com.carpooling.sv.v1.calificacion.domain.Calificacion;

public interface CalificarUsuarioCase {
    Calificacion calificarUsuario(Calificacion calificacion);
}
