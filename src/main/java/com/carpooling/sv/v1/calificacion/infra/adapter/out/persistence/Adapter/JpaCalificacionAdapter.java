package com.carpooling.sv.v1.calificacion.infra.adapter.out.persistence.Adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.calificacion.application.port.out.CalificacionRepositoryPort;
import com.carpooling.sv.v1.calificacion.domain.Calificacion;
import com.carpooling.sv.v1.calificacion.infra.adapter.out.persistence.Jpa.SpringDataCalificacionRepository;
import com.carpooling.sv.v1.calificacion.infra.adapter.out.persistence.entity.JpaCalificacion;

@Component
public class JpaCalificacionAdapter implements CalificacionRepositoryPort {

    private final SpringDataCalificacionRepository repository;

    public JpaCalificacionAdapter(SpringDataCalificacionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Calificacion save(Calificacion calificacion) {
        JpaCalificacion jpa = new JpaCalificacion(
            calificacion.id(),
            calificacion.viajeId(),
            calificacion.evaluadorId(),
            calificacion.evaluadoId(),
            (short) calificacion.puntuacion(),
            calificacion.comentario(),
            calificacion.creadoEn() != null ? calificacion.creadoEn() : OffsetDateTime.now()
        );
        JpaCalificacion saved = repository.save(jpa);
        return toDomain(saved);
    }

    @Override
    public Optional<Calificacion> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Calificacion> findByEvaluadoId(UUID evaluadoId) {
        return repository.findByEvaluadoIdOrderByCreadoEnDesc(evaluadoId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean existsByViajeIdAndEvaluadorIdAndEvaluadoId(UUID viajeId, UUID evaluadorId, UUID evaluadoId) {
        return repository.existsByViajeIdAndEvaluadorIdAndEvaluadoId(viajeId, evaluadorId, evaluadoId);
    }

    private Calificacion toDomain(JpaCalificacion jpa) {
        return new Calificacion(
            jpa.getId(),
            jpa.getViajeId(),
            jpa.getEvaluadorId(),
            jpa.getEvaluadoId(),
            jpa.getPuntuacion(),
            jpa.getComentario(),
            jpa.getCreadoEn()
        );
    }
}
