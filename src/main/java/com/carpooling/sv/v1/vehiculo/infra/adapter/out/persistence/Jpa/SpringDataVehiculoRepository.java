package com.carpooling.sv.v1.vehiculo.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.vehiculo.infra.adapter.out.persistence.entity.JpaVehiculo;

@Repository
public interface SpringDataVehiculoRepository extends JpaRepository<JpaVehiculo, UUID> {

    Optional<JpaVehiculo> findByIdAndActivoTrue(UUID id);

    List<JpaVehiculo> findByPropietarioIdAndActivoTrue(UUID propietarioId);

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, UUID id);
}
