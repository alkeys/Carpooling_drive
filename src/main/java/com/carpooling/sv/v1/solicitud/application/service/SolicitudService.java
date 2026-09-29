package com.carpooling.sv.v1.solicitud.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.shared.exception.DuplicateResourceException;
import com.carpooling.sv.v1.shared.exception.ForbiddenException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.solicitud.application.port.in.CancelarSolicitudCase;
import com.carpooling.sv.v1.solicitud.application.port.in.GetSolicitudByIdCase;
import com.carpooling.sv.v1.solicitud.application.port.in.GetSolicitudesPorPasajeroCase;
import com.carpooling.sv.v1.solicitud.application.port.in.GetSolicitudesPorViajeCase;
import com.carpooling.sv.v1.solicitud.application.port.in.ResponderSolicitudCase;
import com.carpooling.sv.v1.solicitud.application.port.in.SolicitarCupoCase;
import com.carpooling.sv.v1.solicitud.application.port.out.SolicitudRepositoryPort;
import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;
import com.carpooling.sv.v1.solicitud.domain.value.EstadoSolicitud;
import com.carpooling.sv.v1.viaje.application.port.out.ViajeRepositoryPort;
import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;

@Service
@Transactional
public class SolicitudService implements
        SolicitarCupoCase,
        GetSolicitudesPorPasajeroCase,
        GetSolicitudesPorViajeCase,
        GetSolicitudByIdCase,
        ResponderSolicitudCase,
        CancelarSolicitudCase {

    private final SolicitudRepositoryPort solicitudRepositoryPort;
    private final ViajeRepositoryPort viajeRepositoryPort;
    private final SecurityContextHelper securityHelper;

    public SolicitudService(
            SolicitudRepositoryPort solicitudRepositoryPort,
            ViajeRepositoryPort viajeRepositoryPort,
            SecurityContextHelper securityHelper) {
        this.solicitudRepositoryPort = solicitudRepositoryPort;
        this.viajeRepositoryPort = viajeRepositoryPort;
        this.securityHelper = securityHelper;
    }

    @Override
    public SolicitudViaje solicitarCupo(SolicitudViaje solicitud) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Viaje viaje = viajeRepositoryPort.findById(solicitud.viajeId())
            .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + solicitud.viajeId()));

        if (viaje.conductorId().equals(currentUserId)) {
            throw new IllegalArgumentException("No puedes solicitar cupos en tu propio viaje");
        }

        if (viaje.estado() != EstadoViaje.PUBLICADO) {
            throw new IllegalStateException("El viaje no está disponible para recibir nuevas solicitudes (" + viaje.estado() + ")");
        }

        if (viaje.cuposDisponibles() < solicitud.cantidadCupos()) {
            throw new IllegalStateException("El viaje solo tiene " + viaje.cuposDisponibles() + " cupo(s) disponible(s)");
        }

        Optional<SolicitudViaje> existenteOpt = solicitudRepositoryPort.findByViajeIdAndPasajeroId(viaje.id(), currentUserId);

        if (existenteOpt.isPresent()) {
            SolicitudViaje existente = existenteOpt.get();
            if (existente.estado() == EstadoSolicitud.PENDIENTE || existente.estado() == EstadoSolicitud.ACEPTADA) {
                throw new DuplicateResourceException("Ya tienes una solicitud activa para este viaje");
            }
            // Si estaba cancelada o rechazada, se reactiva con los nuevos datos
            SolicitudViaje reactivada = existente.withReactivada(
                solicitud.cantidadCupos(),
                solicitud.puntoSubidaId(),
                solicitud.puntoBajadaId(),
                solicitud.mensaje()
            );
            return solicitudRepositoryPort.update(reactivada);
        }

        SolicitudViaje nueva = new SolicitudViaje(
            null,
            viaje.id(),
            currentUserId,
            solicitud.puntoSubidaId(),
            solicitud.puntoBajadaId(),
            solicitud.cantidadCupos(),
            EstadoSolicitud.PENDIENTE,
            solicitud.mensaje(),
            null,
            null,
            null
        );

        return solicitudRepositoryPort.save(nueva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudViaje> getSolicitudesPorPasajero(UUID pasajeroId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el propio pasajero o ADMIN pueden ver su historial de solicitudes
        if (!pasajeroId.equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para ver las solicitudes de otro usuario");
        }

        return solicitudRepositoryPort.findByPasajeroId(pasajeroId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudViaje> getSolicitudesPorViaje(UUID viajeId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Viaje viaje = viajeRepositoryPort.findById(viajeId)
            .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + viajeId));

        // Anti-IDOR: Solo el conductor del viaje o ADMIN pueden ver las solicitudes recibidas
        if (!viaje.conductorId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para ver las solicitudes de este viaje");
        }

        return solicitudRepositoryPort.findByViajeId(viajeId);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudViaje getSolicitudById(UUID id) {
        SolicitudViaje solicitud = solicitudRepositoryPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));

        UUID currentUserId = securityHelper.getCurrentUserId();
        Viaje viaje = viajeRepositoryPort.findById(solicitud.viajeId()).orElse(null);

        boolean esPasajero = solicitud.pasajeroId().equals(currentUserId);
        boolean esConductor = viaje != null && viaje.conductorId().equals(currentUserId);

        // Anti-IDOR: Solo el pasajero solicitante, el conductor del viaje o ADMIN pueden consultar la solicitud
        if (!esPasajero && !esConductor && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para ver esta solicitud");
        }

        return solicitud;
    }

    @Override
    public SolicitudViaje responderSolicitud(UUID solicitudId, boolean aceptar) {
        SolicitudViaje solicitud = solicitudRepositoryPort.findById(solicitudId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        UUID currentUserId = securityHelper.getCurrentUserId();
        Viaje viaje = viajeRepositoryPort.findById(solicitud.viajeId())
            .orElseThrow(() -> new ResourceNotFoundException("Viaje asociado no encontrado"));

        // Anti-IDOR: Solo el conductor del viaje o ADMIN pueden responder solicitudes
        if (!viaje.conductorId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("Solo el conductor de este viaje puede aceptar o rechazar solicitudes");
        }

        if (aceptar) {
            if (viaje.cuposDisponibles() < solicitud.cantidadCupos()) {
                throw new IllegalStateException("No hay suficientes cupos disponibles en el viaje para aceptar esta solicitud");
            }
            // Descontar cupos de manera atómica
            Viaje viajeActualizado = viaje.withDescontarCupos(solicitud.cantidadCupos());
            viajeRepositoryPort.update(viajeActualizado);

            SolicitudViaje aceptada = solicitud.withAceptada();
            return solicitudRepositoryPort.update(aceptada);
        } else {
            SolicitudViaje rechazada = solicitud.withRechazada();
            return solicitudRepositoryPort.update(rechazada);
        }
    }

    @Override
    public SolicitudViaje cancelarSolicitud(UUID solicitudId) {
        SolicitudViaje solicitud = solicitudRepositoryPort.findById(solicitudId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el pasajero que solicitó o ADMIN pueden cancelar la solicitud
        if (!solicitud.pasajeroId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para cancelar la solicitud de otro usuario");
        }

        // Si ya había sido aceptada, se devuelven los cupos al viaje
        if (solicitud.estado() == EstadoSolicitud.ACEPTADA) {
            viajeRepositoryPort.findById(solicitud.viajeId()).ifPresent(viaje -> {
                Viaje viajeRestaurado = viaje.withRestaurarCupos(solicitud.cantidadCupos());
                viajeRepositoryPort.update(viajeRestaurado);
            });
        }

        SolicitudViaje cancelada = solicitud.withCancelada();
        return solicitudRepositoryPort.update(cancelada);
    }
}
