package com.carpooling.sv.v1.mensajeria.infra.adapter.out.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.mensajeria.application.port.out.NotificacionMensajePort;
import com.carpooling.sv.v1.mensajeria.domain.Mensaje;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeResponse;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;

@Component
public class WebSocketNotificacionMensajeAdapter implements NotificacionMensajePort {

    private final SimpMessagingTemplate messagingTemplate;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public WebSocketNotificacionMensajeAdapter(
            SimpMessagingTemplate messagingTemplate,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        this.messagingTemplate = messagingTemplate;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public void notificarNuevoMensaje(Mensaje mensaje) {
        String remitenteNombre = "Usuario";
        String remitenteRol = "USUARIO";

        if (mensaje.remitenteId() != null) {
            var usuarioOpt = usuarioRepositoryPort.findById(mensaje.remitenteId());
            if (usuarioOpt.isPresent()) {
                var usuario = usuarioOpt.get();
                remitenteNombre = usuario.nombre();
                if (usuario.roles() != null) {
                    if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.ADMIN)) {
                        remitenteRol = "ADMIN";
                    } else if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CONDUCTOR)) {
                        remitenteRol = "CONDUCTOR";
                    } else if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CLIENTE)) {
                        remitenteRol = "CLIENTE";
                    }
                }
            }
        }

        MensajeResponse payload = MensajeResponse.fromDomain(mensaje, remitenteNombre, remitenteRol);
        // Canal de difusión en tiempo real por conversación: /topic/conversaciones/{conversacionId}
        messagingTemplate.convertAndSend("/topic/conversaciones/" + mensaje.conversacionId(), payload);
        // Canal de difusión global para monitoreo de la mesa de soporte del admin
        messagingTemplate.convertAndSend("/topic/conversaciones", payload);
    }
}
