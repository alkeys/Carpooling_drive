package com.carpooling.sv.v1.vehiculo.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;

public interface VehiculoRepositoryPort {
    Vehiculo save(Vehiculo vehiculo);
    Optional<Vehiculo> findById(UUID id);
    Optional<Vehiculo> findByIdAndActivoTrue(UUID id);
    List<Vehiculo> findByPropietarioIdAndActivoTrue(UUID propietarioId);
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, UUID id);
    Vehiculo update(Vehiculo vehiculo);
}
