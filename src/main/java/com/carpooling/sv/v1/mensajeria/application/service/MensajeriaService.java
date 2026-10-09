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
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.ConversacionAdminResponse;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeResponse;
import com.carpooling.sv.v1.shared.exception.ForbiddenException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.solicitud.application.port.out.SolicitudRepositoryPort;
import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;
import com.carpooling.sv.v1.solicitud.domain.value.EstadoSolicitud;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
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
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SecurityContextHelper securityHelper;

    public MensajeriaService(
            MensajeriaRepositoryPort mensajeriaRepositoryPort,
            ViajeRepositoryPort viajeRepositoryPort,
            SolicitudRepositoryPort solicitudRepositoryPort,
            NotificacionMensajePort notificacionMensajePort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            SecurityContextHelper securityHelper) {
        this.mensajeriaRepositoryPort = mensajeriaRepositoryPort;
        this.viajeRepositoryPort = viajeRepositoryPort;
        this.solicitudRepositoryPort = solicitudRepositoryPort;
        this.notificacionMensajePort = notificacionMensajePort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
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

        // Registrar como participante si aún no lo está
        if (!mensajeriaRepositoryPort.esParticipante(conversacion.id(), currentUserId)) {
            mensajeriaRepositoryPort.agregarParticipante(conversacion.id(), currentUserId);
        }

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

    @Transactional(readOnly = true)
    public List<MensajeResponse> getMensajesDetallados(UUID conversacionId) {
        List<Mensaje> mensajes = getMensajesPorConversacion(conversacionId);
        return mensajes.stream().map(m -> {
            String remitenteNombre = "Usuario";
            String remitenteRol = "USUARIO";
            if (m.remitenteId() != null) {
                var u = usuarioRepositoryPort.findById(m.remitenteId());
                if (u.isPresent()) {
                    var usuario = u.get();
                    remitenteNombre = usuario.nombre();
                    remitenteRol = resolverRolPrincipal(usuario);
                }
            }
            return MensajeResponse.fromDomain(m, remitenteNombre, remitenteRol);
        }).toList();
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

    @Transactional(readOnly = true)
    public List<ConversacionAdminResponse> getTodasConversacionesAdmin() {
        if (!securityHelper.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden consultar todas las conversaciones");
        }

        List<Conversacion> todas = mensajeriaRepositoryPort.findAllConversaciones();
        return todas.stream().map(c -> {
            String origen = "Ruta personalizada";
            String destino = "Destino";
            UUID conductorId = null;
            String conductorNombre = "Conductor Rutea";
            String conductorCorreo = "";

            var viajeOpt = viajeRepositoryPort.findById(c.viajeId());
            if (viajeOpt.isPresent()) {
                var v = viajeOpt.get();
                if (v.origenNombre() != null) {
                    origen = v.origenNombre();
                }
                if (v.destinoNombre() != null) {
                    destino = v.destinoNombre();
                }
                conductorId = v.conductorId();
                if (conductorId != null) {
                    var condOpt = usuarioRepositoryPort.findById(conductorId);
                    if (condOpt.isPresent()) {
                        conductorNombre = condOpt.get().nombre();
                        conductorCorreo = condOpt.get().correo();
                    }
                }
            }

            var ultimoOpt = mensajeriaRepositoryPort.findUltimoMensajePorConversacionId(c.id());
            String ultimoContenido = ultimoOpt.map(Mensaje::contenido).orElse("Sin mensajes registrados");
            java.time.OffsetDateTime ultimoEn = ultimoOpt.map(Mensaje::enviadoEn).orElse(c.creadoEn());
            long total = mensajeriaRepositoryPort.countMensajesPorConversacionId(c.id());

            return new ConversacionAdminResponse(
                c.id(),
                c.viajeId(),
                origen,
                destino,
                conductorId,
                conductorNombre,
                conductorCorreo,
                c.creadoEn(),
                c.participantesIds(),
                ultimoContenido,
                ultimoEn,
                total
            );
        }).toList();
    }

    private String resolverRolPrincipal(com.carpooling.sv.v1.usuario.domain.Usuario usuario) {
        if (usuario == null || usuario.roles() == null || usuario.roles().isEmpty()) {
            return "USUARIO";
        }
        if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.ADMIN)) return "ADMIN";
        if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CONDUCTOR)) return "CONDUCTOR";
        if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CLIENTE)) return "CLIENTE";
        return usuario.roles().iterator().next().name();
    }

    private void asegurarParticipacion(Conversacion conversacion, UUID usuarioId) {
        if (securityHelper.isAdmin()) {
            return;
        }

        if (mensajeriaRepositoryPort.esParticipante(conversacion.id(), usuarioId)) {
            return;
        }

        // Si no está registrado formalmente, verificar si es conductor o usuario autenticado
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

        // 2. Es un pasajero con solicitud o interesado en la ruta
        Optional<SolicitudViaje> solicitud = solicitudRepositoryPort.findByViajeIdAndPasajeroId(viaje.id(), usuarioId);
        if (solicitud.isPresent()) {
            return;
        }

        // Permitir a usuarios autenticados consultar al conductor del viaje
        if (usuarioId != null) {
            return;
        }

        throw new ForbiddenException("Solo el conductor y los pasajeros confirmados tienen acceso al chat de este viaje");
    }
}
