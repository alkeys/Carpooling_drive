package com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.Adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.solicitud.application.port.out.SolicitudRepositoryPort;
import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;
import com.carpooling.sv.v1.solicitud.domain.value.EstadoSolicitud;
import com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.Jpa.SpringDataSolicitudRepository;
import com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.entity.JpaSolicitudViaje;
import com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.entity.value.EstadoSolicitudEnum;

@Component
public class JpaSolicitudAdapter implements SolicitudRepositoryPort {

    private final SpringDataSolicitudRepository repository;

    public JpaSolicitudAdapter(SpringDataSolicitudRepository repository) {
        this.repository = repository;
    }

    @Override
    public SolicitudViaje save(SolicitudViaje solicitud) {
        JpaSolicitudViaje jpa = toJpa(solicitud);
        JpaSolicitudViaje saved = repository.save(jpa);
        return toDomain(saved);
    }

    @Override
    public Optional<SolicitudViaje> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SolicitudViaje> findByViajeIdAndPasajeroId(UUID viajeId, UUID pasajeroId) {
        return repository.findByViajeIdAndPasajeroId(viajeId, pasajeroId).map(this::toDomain);
    }

    @Override
    public List<SolicitudViaje> findByPasajeroId(UUID pasajeroId) {
        return repository.findByPasajeroIdOrderByCreadoEnDesc(pasajeroId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<SolicitudViaje> findByViajeId(UUID viajeId) {
        return repository.findByViajeIdOrderByCreadoEnDesc(viajeId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public SolicitudViaje update(SolicitudViaje solicitud) {
        JpaSolicitudViaje jpa = toJpa(solicitud);
        JpaSolicitudViaje updated = repository.save(jpa);
        return toDomain(updated);
    }

    private JpaSolicitudViaje toJpa(SolicitudViaje domain) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new JpaSolicitudViaje(
            domain.id(),
            domain.viajeId(),
            domain.pasajeroId(),
            domain.puntoSubidaId(),
            domain.puntoBajadaId(),
            (short) domain.cantidadCupos(),
            EstadoSolicitudEnum.valueOf(domain.estado().name()),
            domain.mensaje(),
            domain.creadoEn() != null ? domain.creadoEn() : ahora,
            domain.respondidoEn(),
            ahora
        );
    }

    private SolicitudViaje toDomain(JpaSolicitudViaje jpa) {
        return new SolicitudViaje(
            jpa.getId(),
            jpa.getViajeId(),
            jpa.getPasajeroId(),
            jpa.getPuntoSubidaId(),
            jpa.getPuntoBajadaId(),
            jpa.getCantidadCupos(),
            EstadoSolicitud.valueOf(jpa.getEstado().name()),
            jpa.getMensaje(),
            jpa.getCreadoEn(),
            jpa.getRespondidoEn(),
            jpa.getActualizadoEn()
        );
    }
}
