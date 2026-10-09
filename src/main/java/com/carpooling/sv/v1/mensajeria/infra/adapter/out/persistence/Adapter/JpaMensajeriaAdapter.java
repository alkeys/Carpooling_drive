package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.mensajeria.application.port.out.MensajeriaRepositoryPort;
import com.carpooling.sv.v1.mensajeria.domain.Conversacion;
import com.carpooling.sv.v1.mensajeria.domain.Mensaje;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Jpa.SpringDataConversacionRepository;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Jpa.SpringDataMensajeRepository;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.Jpa.SpringDataParticipanteRepository;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.JpaConversacion;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.JpaMensaje;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.JpaParticipanteConversacion;
import com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity.ParticipanteConversacionId;

@Component
public class JpaMensajeriaAdapter implements MensajeriaRepositoryPort {

    private final SpringDataConversacionRepository conversacionRepository;
    private final SpringDataParticipanteRepository participanteRepository;
    private final SpringDataMensajeRepository mensajeRepository;

    public JpaMensajeriaAdapter(
            SpringDataConversacionRepository conversacionRepository,
            SpringDataParticipanteRepository participanteRepository,
            SpringDataMensajeRepository mensajeRepository) {
        this.conversacionRepository = conversacionRepository;
        this.participanteRepository = participanteRepository;
        this.mensajeRepository = mensajeRepository;
    }

    @Override
    public Conversacion saveConversacion(Conversacion conversacion) {
        JpaConversacion jpa = new JpaConversacion(
            conversacion.id(),
            conversacion.viajeId(),
            conversacion.creadoEn() != null ? conversacion.creadoEn() : OffsetDateTime.now()
        );
        JpaConversacion saved = conversacionRepository.save(jpa);
        return toDomain(saved);
    }

    @Override
    public Optional<Conversacion> findConversacionById(UUID id) {
        return conversacionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Conversacion> findConversacionByViajeId(UUID viajeId) {
        return conversacionRepository.findByViajeId(viajeId).map(this::toDomain);
    }

    @Override
    public List<Conversacion> findConversacionesByUsuarioId(UUID usuarioId) {
        return conversacionRepository.findByUsuarioId(usuarioId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<Conversacion> findAllConversaciones() {
        return conversacionRepository.findAllByOrderByCreadoEnDesc().stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public void agregarParticipante(UUID conversacionId, UUID usuarioId) {
        ParticipanteConversacionId id = new ParticipanteConversacionId(conversacionId, usuarioId);
        if (!participanteRepository.existsById(id)) {
            JpaParticipanteConversacion part = new JpaParticipanteConversacion(id, OffsetDateTime.now());
            participanteRepository.save(part);
        }
    }

    @Override
    public boolean esParticipante(UUID conversacionId, UUID usuarioId) {
        return participanteRepository.existsByIdConversacionIdAndIdUsuarioId(conversacionId, usuarioId);
    }

    @Override
    public List<UUID> findParticipantesIds(UUID conversacionId) {
        return participanteRepository.findUsuarioIdsByConversacionId(conversacionId);
    }

    @Override
    public Mensaje saveMensaje(Mensaje mensaje) {
        JpaMensaje jpa = new JpaMensaje(
            mensaje.id(),
            mensaje.conversacionId(),
            mensaje.remitenteId(),
            mensaje.contenido(),
            mensaje.enviadoEn() != null ? mensaje.enviadoEn() : OffsetDateTime.now(),
            mensaje.leidoEn()
        );
        JpaMensaje saved = mensajeRepository.save(jpa);
        return toDomain(saved);
    }

    @Override
    public List<Mensaje> findMensajesByConversacionId(UUID conversacionId) {
        return mensajeRepository.findByConversacionIdOrderByEnviadoEnAsc(conversacionId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public Optional<Mensaje> findUltimoMensajePorConversacionId(UUID conversacionId) {
        return mensajeRepository.findFirstByConversacionIdOrderByEnviadoEnDesc(conversacionId)
            .map(this::toDomain);
    }

    @Override
    public long countMensajesPorConversacionId(UUID conversacionId) {
        return mensajeRepository.countByConversacionId(conversacionId);
    }

    private Conversacion toDomain(JpaConversacion jpa) {
        List<UUID> participantes = participanteRepository.findUsuarioIdsByConversacionId(jpa.getId());
        return new Conversacion(jpa.getId(), jpa.getViajeId(), jpa.getCreadoEn(), participantes);
    }

    private Mensaje toDomain(JpaMensaje jpa) {
        return new Mensaje(
            jpa.getId(),
            jpa.getConversacionId(),
            jpa.getRemitenteId(),
            jpa.getContenido(),
            jpa.getEnviadoEn(),
            jpa.getLeidoEn()
        );
    }
}
