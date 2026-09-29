package com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.JpaPuntoEncuentro;

@Repository
public interface SpringDataPuntoEncuentroRepository extends JpaRepository<JpaPuntoEncuentro, UUID> {

    List<JpaPuntoEncuentro> findByViajeIdOrderByOrdenAsc(UUID viajeId);

    void deleteByViajeId(UUID viajeId);
}
