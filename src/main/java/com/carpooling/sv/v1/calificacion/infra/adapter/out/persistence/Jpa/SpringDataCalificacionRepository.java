package com.carpooling.sv.v1.calificacion.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.calificacion.infra.adapter.out.persistence.entity.JpaCalificacion;

@Repository
public interface SpringDataCalificacionRepository extends JpaRepository<JpaCalificacion, UUID> {

    List<JpaCalificacion> findByEvaluadoIdOrderByCreadoEnDesc(UUID evaluadoId);

    boolean existsByViajeIdAndEvaluadorIdAndEvaluadoId(UUID viajeId, UUID evaluadorId, UUID evaluadoId);
}
