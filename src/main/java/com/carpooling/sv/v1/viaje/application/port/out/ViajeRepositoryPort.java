package com.carpooling.sv.v1.viaje.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.PuntoEncuentro;
import com.carpooling.sv.v1.viaje.domain.Viaje;

public interface ViajeRepositoryPort {
    Viaje save(Viaje viaje);
    Optional<Viaje> findById(UUID id);
    List<Viaje> findByConductorId(UUID conductorId);
    List<Viaje> buscarViajesGeo(
        Double origenLon,
        Double origenLat,
        Double radioOrigenMetros,
        Double destinoLon,
        Double destinoLat,
        Double radioDestinoMetros,
        LocalDate fecha,
        int cuposMinimos
    );
    List<Viaje> buscarViajesTexto(String origenTexto, String destinoTexto, LocalDate fecha, int cuposMinimos);
    Viaje update(Viaje viaje);
    PuntoEncuentro savePuntoEncuentro(PuntoEncuentro punto);
    List<PuntoEncuentro> findPuntosPorViajeId(UUID viajeId);
    void deletePuntosPorViajeId(UUID viajeId);
}
