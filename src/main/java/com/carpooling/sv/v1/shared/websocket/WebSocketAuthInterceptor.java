package com.carpooling.sv.v1.shared.websocket;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.carpooling.sv.v1.shared.security.CustomUserDetailsService;
import com.carpooling.sv.v1.shared.security.JwtService;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);

            if (StringUtils.hasText(token) && jwtService.validateToken(token)) {
                String correo = jwtService.extractCorreo(token);
                if (correo != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("WebSocket STOMP conectado y autenticado para usuario: {}", correo);
                    } catch (Exception ex) {
                        log.warn("Fallo al autenticar sesión de WebSocket para correo: {}", correo, ex);
                    }
                }
            } else {
                log.warn("Intento de conexión STOMP sin token JWT válido");
            }
        }

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        // 1. Header Authorization estándar en frame STOMP
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            authHeaders = accessor.getNativeHeader("authorization");
        }

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearer = authHeaders.get(0);
            if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                return bearer.substring(7);
            }
            return bearer;
        }

        // 2. Parámetro 'token' o 'access_token' en encabezados nativos
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        List<String> accessTokenHeaders = accessor.getNativeHeader("access_token");
        if (accessTokenHeaders != null && !accessTokenHeaders.isEmpty()) {
            return accessTokenHeaders.get(0);
        }

        return null;
    }
}
