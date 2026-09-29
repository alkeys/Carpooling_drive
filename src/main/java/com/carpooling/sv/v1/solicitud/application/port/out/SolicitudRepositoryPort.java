package com.carpooling.sv.v1.solicitud.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;

public interface SolicitudRepositoryPort {
    SolicitudViaje save(SolicitudViaje solicitud);
    Optional<SolicitudViaje> findById(UUID id);
    Optional<SolicitudViaje> findByViajeIdAndPasajeroId(UUID viajeId, UUID pasajeroId);
    List<SolicitudViaje> findByPasajeroId(UUID pasajeroId);
    List<SolicitudViaje> findByViajeId(UUID viajeId);
    SolicitudViaje update(SolicitudViaje solicitud);
}
