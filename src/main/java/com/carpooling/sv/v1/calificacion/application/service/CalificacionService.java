package com.carpooling.sv.v1.calificacion.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.calificacion.application.port.in.CalificarUsuarioCase;
import com.carpooling.sv.v1.calificacion.application.port.in.GetCalificacionesPorUsuarioCase;
import com.carpooling.sv.v1.calificacion.application.port.in.GetReputacionUsuarioCase;
import com.carpooling.sv.v1.calificacion.application.port.out.CalificacionRepositoryPort;
import com.carpooling.sv.v1.calificacion.domain.Calificacion;
import com.carpooling.sv.v1.calificacion.domain.ReputacionUsuario;
import com.carpooling.sv.v1.shared.exception.DuplicateResourceException;
import com.carpooling.sv.v1.shared.exception.ForbiddenException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.solicitud.application.port.out.SolicitudRepositoryPort;
import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;
import com.carpooling.sv.v1.solicitud.domain.value.EstadoSolicitud;
import com.carpooling.sv.v1.viaje.application.port.out.ViajeRepositoryPort;
import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;

@Service
@Transactional
public class CalificacionService implements
        CalificarUsuarioCase,
        GetCalificacionesPorUsuarioCase,
        GetReputacionUsuarioCase {

    private final CalificacionRepositoryPort calificacionRepositoryPort;
    private final ViajeRepositoryPort viajeRepositoryPort;
    private final SolicitudRepositoryPort solicitudRepositoryPort;
    private final SecurityContextHelper securityHelper;

    public CalificacionService(
            CalificacionRepositoryPort calificacionRepositoryPort,
            ViajeRepositoryPort viajeRepositoryPort,
            SolicitudRepositoryPort solicitudRepositoryPort,
            SecurityContextHelper securityHelper) {
        this.calificacionRepositoryPort = calificacionRepositoryPort;
        this.viajeRepositoryPort = viajeRepositoryPort;
        this.solicitudRepositoryPort = solicitudRepositoryPort;
        this.securityHelper = securityHelper;
    }

    @Override
    public Calificacion calificarUsuario(Calificacion calificacion) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el usuario autenticado puede emitir una calificación en su nombre
        if (!calificacion.evaluadorId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No puedes emitir una calificación en nombre de otro usuario");
        }

        if (calificacion.evaluadorId().equals(calificacion.evaluadoId())) {
            throw new IllegalArgumentException("No puedes calificarte a ti mismo");
        }

        Viaje viaje = viajeRepositoryPort.findById(calificacion.viajeId())
            .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + calificacion.viajeId()));

        if (viaje.estado() != EstadoViaje.FINALIZADO && viaje.estado() != EstadoViaje.EN_CURSO) {
            throw new IllegalStateException("Solo se puede calificar después de que el viaje haya iniciado o finalizado");
        }

        // Verificar que ambos usuarios compartieron el viaje
        verificarParticipacionMutua(viaje, calificacion.evaluadorId(), calificacion.evaluadoId());

        // Verificar que no haya calificado ya al mismo usuario en el mismo viaje
        if (calificacionRepositoryPort.existsByViajeIdAndEvaluadorIdAndEvaluadoId(
                viaje.id(), calificacion.evaluadorId(), calificacion.evaluadoId())) {
            throw new DuplicateResourceException("Ya calificaste a este usuario en este viaje");
        }

        return calificacionRepositoryPort.save(calificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Calificacion> getCalificacionesPorUsuario(UUID usuarioId) {
        return calificacionRepositoryPort.findByEvaluadoId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReputacionUsuario getReputacionPorUsuario(UUID usuarioId) {
        List<Calificacion> todas = calificacionRepositoryPort.findByEvaluadoId(usuarioId);

        if (todas.isEmpty()) {
            return new ReputacionUsuario(usuarioId, 5.0, 0, List.of());
        }

        double promedio = todas.stream()
            .mapToInt(Calificacion::puntuacion)
            .average()
            .orElse(5.0);

        BigDecimal rounded = BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);

        List<Calificacion> ultimas = todas.stream()
            .limit(10)
            .toList();

        return new ReputacionUsuario(usuarioId, rounded.doubleValue(), todas.size(), ultimas);
    }

    private void verificarParticipacionMutua(Viaje viaje, UUID evaluadorId, UUID evaluadoId) {
        if (securityHelper.isAdmin()) {
            return;
        }

        boolean evaluadorEsConductor = viaje.conductorId().equals(evaluadorId);
        boolean evaluadoEsConductor = viaje.conductorId().equals(evaluadoId);

        boolean evaluadorEsPasajero = esPasajeroAceptado(viaje.id(), evaluadorId);
        boolean evaluadoEsPasajero = esPasajeroAceptado(viaje.id(), evaluadoId);

        boolean evaluadorParticipo = evaluadorEsConductor || evaluadorEsPasajero;
        boolean evaluadoParticipo = evaluadoEsConductor || evaluadoEsPasajero;

        if (!evaluadorParticipo || !evaluadoParticipo) {
            throw new ForbiddenException("Ambos usuarios deben haber participado en el viaje para poder calificarse");
        }
    }

    private boolean esPasajeroAceptado(UUID viajeId, UUID usuarioId) {
        Optional<SolicitudViaje> sol = solicitudRepositoryPort.findByViajeIdAndPasajeroId(viajeId, usuarioId);
        return sol.isPresent() && sol.get().estado() == EstadoSolicitud.ACEPTADA;
    }
}
