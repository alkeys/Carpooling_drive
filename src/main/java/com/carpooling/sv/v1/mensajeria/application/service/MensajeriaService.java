package com.carpooling.sv.v1.mensajeria.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.mensajeria.application.port.in.CrearOObtenerConversacionViajeCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.EnviarMensajeCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.GetConversacionesUsuarioCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.GetMensajesConversacionCase;
import com.carpooling.sv.v1.mensajeria.application.port.out.MensajeriaRepositoryPort;
import com.carpooling.sv.v1.mensajeria.application.port.out.NotificacionMensajePort;
import com.carpooling.sv.v1.mensajeria.domain.Conversacion;
import com.carpooling.sv.v1.mensajeria.domain.Mensaje;
import com.carpooling.sv.v1.shared.exception.ForbiddenException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.solicitud.application.port.out.SolicitudRepositoryPort;
import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;
import com.carpooling.sv.v1.solicitud.domain.value.EstadoSolicitud;
import com.carpooling.sv.v1.viaje.application.port.out.ViajeRepositoryPort;
import com.carpooling.sv.v1.viaje.domain.Viaje;

@Service
@Transactional
public class MensajeriaService implements
        CrearOObtenerConversacionViajeCase,
        EnviarMensajeCase,
        GetMensajesConversacionCase,
        GetConversacionesUsuarioCase {

    private final MensajeriaRepositoryPort mensajeriaRepositoryPort;
    private final ViajeRepositoryPort viajeRepositoryPort;
    private final SolicitudRepositoryPort solicitudRepositoryPort;
    private final NotificacionMensajePort notificacionMensajePort;
    private final SecurityContextHelper securityHelper;

    public MensajeriaService(
            MensajeriaRepositoryPort mensajeriaRepositoryPort,
            ViajeRepositoryPort viajeRepositoryPort,
            SolicitudRepositoryPort solicitudRepositoryPort,
            NotificacionMensajePort notificacionMensajePort,
            SecurityContextHelper securityHelper) {
        this.mensajeriaRepositoryPort = mensajeriaRepositoryPort;
        this.viajeRepositoryPort = viajeRepositoryPort;
        this.solicitudRepositoryPort = solicitudRepositoryPort;
        this.notificacionMensajePort = notificacionMensajePort;
        this.securityHelper = securityHelper;
    }

    @Override
    public Conversacion obtenerOCrearConversacionPorViaje(UUID viajeId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Viaje viaje = viajeRepositoryPort.findById(viajeId)
            .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + viajeId));

        verificarAccesoAViaje(viaje, currentUserId);

        Optional<Conversacion> existente = mensajeriaRepositoryPort.findConversacionByViajeId(viajeId);
        Conversacion conversacion;
        if (existente.isPresent()) {
            conversacion = existente.get();
        } else {
            Conversacion nueva = new Conversacion(null, viajeId, null, List.of(viaje.conductorId()));
            conversacion = mensajeriaRepositoryPort.saveConversacion(nueva);
            mensajeriaRepositoryPort.agregarParticipante(conversacion.id(), viaje.conductorId());
        }

        // Asegurar que el usuario actual quede registrado como participante
        if (!mensajeriaRepositoryPort.esParticipante(conversacion.id(), currentUserId)) {
            mensajeriaRepositoryPort.agregarParticipante(conversacion.id(), currentUserId);
        }

        List<UUID> participantes = mensajeriaRepositoryPort.findParticipantesIds(conversacion.id());
        return new Conversacion(conversacion.id(), conversacion.viajeId(), conversacion.creadoEn(), participantes);
    }

    @Override
    public Mensaje enviarMensaje(UUID conversacionId, String contenido) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Conversacion conversacion = mensajeriaRepositoryPort.findConversacionById(conversacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada con ID: " + conversacionId));

        asegurarParticipacion(conversacion, currentUserId);

        Mensaje mensaje = new Mensaje(
            null,
            conversacionId,
            currentUserId,
            contenido,
            null,
            null
        );

        Mensaje guardado = mensajeriaRepositoryPort.saveMensaje(mensaje);
        notificacionMensajePort.notificarNuevoMensaje(guardado);
        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mensaje> getMensajesPorConversacion(UUID conversacionId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Conversacion conversacion = mensajeriaRepositoryPort.findConversacionById(conversacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada con ID: " + conversacionId));

        asegurarParticipacion(conversacion, currentUserId);

        return mensajeriaRepositoryPort.findMensajesByConversacionId(conversacionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversacion> getConversacionesPorUsuario(UUID usuarioId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el propio usuario o ADMIN pueden consultar su lista de conversaciones
        if (!usuarioId.equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para ver las conversaciones de otro usuario");
        }

        return mensajeriaRepositoryPort.findConversacionesByUsuarioId(usuarioId);
    }

    private void asegurarParticipacion(Conversacion conversacion, UUID usuarioId) {
        if (securityHelper.isAdmin()) {
            return;
        }

        if (mensajeriaRepositoryPort.esParticipante(conversacion.id(), usuarioId)) {
            return;
        }

        // Si no está registrado formalmente, verificar si es conductor o pasajero aceptado del viaje
        Viaje viaje = viajeRepositoryPort.findById(conversacion.viajeId()).orElse(null);
        if (viaje != null) {
            verificarAccesoAViaje(viaje, usuarioId);
            mensajeriaRepositoryPort.agregarParticipante(conversacion.id(), usuarioId);
            return;
        }

        throw new ForbiddenException("No tienes permiso para acceder a esta conversación");
    }

    private void verificarAccesoAViaje(Viaje viaje, UUID usuarioId) {
        if (securityHelper.isAdmin()) {
            return;
        }

        // 1. Es el conductor
        if (viaje.conductorId().equals(usuarioId)) {
            return;
        }

        // 2. Es un pasajero con solicitud aceptada
        Optional<SolicitudViaje> solicitud = solicitudRepositoryPort.findByViajeIdAndPasajeroId(viaje.id(), usuarioId);
        if (solicitud.isPresent() && solicitud.get().estado() == EstadoSolicitud.ACEPTADA) {
            return;
        }

        throw new ForbiddenException("Solo el conductor y los pasajeros confirmados tienen acceso al chat de este viaje");
    }
}
