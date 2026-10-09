package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.JpaMensaje;

@Repository
public interface SpringDataMensajeRepository extends JpaRepository<JpaMensaje, UUID> {

    List<JpaMensaje> findByConversacionIdOrderByEnviadoEnAsc(UUID conversacionId);

    java.util.Optional<JpaMensaje> findFirstByConversacionIdOrderByEnviadoEnDesc(UUID conversacionId);

    long countByConversacionId(UUID conversacionId);
}
