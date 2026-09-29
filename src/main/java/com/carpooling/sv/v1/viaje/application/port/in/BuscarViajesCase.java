package com.carpooling.sv.v1.viaje.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.carpooling.sv.v1.viaje.domain.Viaje;

public interface BuscarViajesCase {
    List<Viaje> buscarViajes(
        Double origenLat,
        Double origenLon,
        Double radioOrigenKm,
        Double destinoLat,
        Double destinoLon,
        Double radioDestinoKm,
        String origenTexto,
        String destinoTexto,
        LocalDate fecha,
        Integer cuposMinimos
    );
}
