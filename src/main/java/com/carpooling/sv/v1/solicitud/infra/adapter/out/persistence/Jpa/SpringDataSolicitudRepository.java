package com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.entity.JpaSolicitudViaje;

@Repository
public interface SpringDataSolicitudRepository extends JpaRepository<JpaSolicitudViaje, UUID> {

    Optional<JpaSolicitudViaje> findByViajeIdAndPasajeroId(UUID viajeId, UUID pasajeroId);

    List<JpaSolicitudViaje> findByPasajeroIdOrderByCreadoEnDesc(UUID pasajeroId);

    List<JpaSolicitudViaje> findByViajeIdOrderByCreadoEnDesc(UUID viajeId);
}
