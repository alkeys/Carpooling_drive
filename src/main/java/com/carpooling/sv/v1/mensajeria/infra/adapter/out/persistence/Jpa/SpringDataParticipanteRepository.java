package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.JpaParticipanteConversacion;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.ParticipanteConversacionId;

@Repository
public interface SpringDataParticipanteRepository extends JpaRepository<JpaParticipanteConversacion, ParticipanteConversacionId> {

    @Query("SELECT p.id.usuarioId FROM JpaParticipanteConversacion p WHERE p.id.conversacionId = :conversacionId")
    List<UUID> findUsuarioIdsByConversacionId(@Param("conversacionId") UUID conversacionId);

    boolean existsByIdConversacionIdAndIdUsuarioId(UUID conversacionId, UUID usuarioId);
}
