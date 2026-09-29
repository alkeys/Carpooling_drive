package com.carpooling.sv.v1.mensajeria.infra.adapter.out.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.mensajeria.application.port.out.NotificacionMensajePort;
import com.carpooling.sv.v1.mensajeria.domain.Mensaje;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeResponse;

@Component
public class WebSocketNotificacionMensajeAdapter implements NotificacionMensajePort {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificacionMensajeAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notificarNuevoMensaje(Mensaje mensaje) {
        MensajeResponse payload = MensajeResponse.fromDomain(mensaje);
        // Canal de difusión en tiempo real por conversación: /topic/conversaciones/{conversacionId}
        messagingTemplate.convertAndSend("/topic/conversaciones/" + mensaje.conversacionId(), payload);
    }
}
