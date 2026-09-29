package com.carpooling.sv.v1.ruta.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.ruta.infra.adapter.out.persistence.entity.JpaRuta;

@Repository
public interface SpringDataRutaRepository extends JpaRepository<JpaRuta, UUID> {

    Optional<JpaRuta> findByIdAndActivoTrue(UUID id);

    List<JpaRuta> findByActivoTrue();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
}
