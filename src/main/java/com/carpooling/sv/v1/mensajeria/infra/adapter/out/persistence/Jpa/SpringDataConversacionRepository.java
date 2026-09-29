package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.JpaConversacion;

@Repository
public interface SpringDataConversacionRepository extends JpaRepository<JpaConversacion, UUID> {

    Optional<JpaConversacion> findByViajeId(UUID viajeId);

    @Query("SELECT c FROM JpaConversacion c WHERE c.id IN (SELECT p.id.conversacionId FROM JpaParticipanteConversacion p WHERE p.id.usuarioId = :usuarioId) ORDER BY c.creadoEn DESC")
    List<JpaConversacion> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
