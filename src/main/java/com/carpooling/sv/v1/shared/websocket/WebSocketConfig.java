package com.carpooling.sv.v1.shared.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket estándar (para clientes nativos móviles, Postman o librerías WebSocket estándar)
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*");

        // Endpoint con fallback de SockJS (para navegadores web antiguos o proxies restrictivos)
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para suscripciones de clientes (/topic para salas públicas/grupales, /queue para mensajes privados)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para enviar mensajes desde el cliente hacia los controladores de backend (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes dirigidos a un usuario específico
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Interceptor para validar el JWT en el handshake/frame CONNECT de STOMP
        registration.interceptors(webSocketAuthInterceptor);
    }
}
