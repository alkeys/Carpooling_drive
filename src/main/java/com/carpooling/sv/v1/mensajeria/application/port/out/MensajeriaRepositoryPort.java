package com.carpooling.sv.v1.mensajeria.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Conversacion;
import com.carpooling.sv.v1.mensajeria.domain.Mensaje;

public interface MensajeriaRepositoryPort {
    Conversacion saveConversacion(Conversacion conversacion);
    Optional<Conversacion> findConversacionById(UUID id);
    Optional<Conversacion> findConversacionByViajeId(UUID viajeId);
    List<Conversacion> findConversacionesByUsuarioId(UUID usuarioId);
    List<Conversacion> findAllConversaciones();
    void agregarParticipante(UUID conversacionId, UUID usuarioId);
    boolean esParticipante(UUID conversacionId, UUID usuarioId);
    List<UUID> findParticipantesIds(UUID conversacionId);
    Mensaje saveMensaje(Mensaje mensaje);
    List<Mensaje> findMensajesByConversacionId(UUID conversacionId);
    Optional<Mensaje> findUltimoMensajePorConversacionId(UUID conversacionId);
    long countMensajesPorConversacionId(UUID conversacionId);
}
