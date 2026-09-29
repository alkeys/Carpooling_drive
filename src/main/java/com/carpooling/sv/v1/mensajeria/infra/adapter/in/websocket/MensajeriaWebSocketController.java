package com.carpooling.sv.v1.mensajeria.infra.adapter.in.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.carpooling.sv.v1.mensajeria.application.port.in.EnviarMensajeCase;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeEnviarRequest;
import com.carpooling.sv.v1.shared.exception.UnauthorizedException;

import jakarta.validation.Valid;

@Controller
public class MensajeriaWebSocketController {

    private final EnviarMensajeCase enviarMensajeCase;

    public MensajeriaWebSocketController(EnviarMensajeCase enviarMensajeCase) {
        this.enviarMensajeCase = enviarMensajeCase;
    }

    /**
     * Permite enviar un mensaje directamente a través del WebSocket (STOMP).
     * Destino: /app/conversaciones/{conversacionId}/enviar
     */
    @MessageMapping("/conversaciones/{conversacionId}/enviar")
    public void enviarMensajePorWebSocket(
            @DestinationVariable UUID conversacionId,
            @Valid @Payload MensajeEnviarRequest request,
            Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Debes estar autenticado para enviar mensajes por WebSocket");
        }

        enviarMensajeCase.enviarMensaje(conversacionId, request.contenido());
    }
}
