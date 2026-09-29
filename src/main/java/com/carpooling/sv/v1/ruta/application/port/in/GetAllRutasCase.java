package com.carpooling.sv.v1.ruta.application.port.in;

import java.util.List;

import com.carpooling.sv.v1.ruta.domain.Ruta;

public interface GetAllRutasCase {
    List<Ruta> getAllRutas();
}
