package com.carpooling.sv.v1.ruta.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.ruta.domain.Ruta;

public interface ActualizarRutaCase {
    Ruta actualizarRuta(UUID id, Ruta ruta);
}
