package com.carpooling.sv.v1.ruta.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.ruta.domain.Ruta;

public interface CambiarEstadoRutaCase {
    Ruta cambiarEstadoRuta(UUID id, boolean activo);
}
