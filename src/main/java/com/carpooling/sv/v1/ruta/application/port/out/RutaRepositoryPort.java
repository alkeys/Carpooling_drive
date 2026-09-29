package com.carpooling.sv.v1.ruta.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.ruta.domain.Ruta;

public interface RutaRepositoryPort {
    Ruta save(Ruta ruta);
    Optional<Ruta> findById(UUID id);
    Optional<Ruta> findByIdAndActivoTrue(UUID id);
    List<Ruta> findAll();
    List<Ruta> findByActivoTrue();
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, UUID id);
    Ruta update(Ruta ruta);
}
