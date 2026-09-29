package com.carpooling.sv.v1.calificacion.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.calificacion.domain.Calificacion;

public interface CalificacionRepositoryPort {
    Calificacion save(Calificacion calificacion);
    Optional<Calificacion> findById(UUID id);
    List<Calificacion> findByEvaluadoId(UUID evaluadoId);
    boolean existsByViajeIdAndEvaluadorIdAndEvaluadoId(UUID viajeId, UUID evaluadorId, UUID evaluadoId);
}
