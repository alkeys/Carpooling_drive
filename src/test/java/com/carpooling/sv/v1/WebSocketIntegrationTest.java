package com.carpooling.sv.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.carpooling.sv.v1.auth.infra.adapter.in.web.dto.LoginRequest;
import com.carpooling.sv.v1.mensajeria.application.port.in.CrearOObtenerConversacionViajeCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.EnviarMensajeCase;
import com.carpooling.sv.v1.mensajeria.domain.Conversacion;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeResponse;
import com.carpooling.sv.v1.shared.security.JwtService;
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;
import com.carpooling.sv.v1.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;
import com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo;
import com.carpooling.sv.v1.viaje.application.port.in.PublicarViajeCase;
import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Autowired
    private VehiculoRepositoryPort vehiculoRepositoryPort;

    @Autowired
    private PasswordEncoderPort passwordEncoderPort;

    @Autowired
    private PublicarViajeCase publicarViajeCase;

    @Autowired
    private CrearOObtenerConversacionViajeCase crearConversacionCase;

    @Autowired
    private EnviarMensajeCase enviarMensajeCase;

    @Autowired
    private com.carpooling.sv.v1.shared.security.CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketStompClient stompClient;

    private static final String WS_USER_EMAIL = "ws.conductor@carpooling.sv";
    private static final String COMMON_PASS = "PasswordWs123";

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @Test
    void shouldConnectToWebSocketWithJwtAndReceiveRealTimeMessages() throws Exception {
        // 1. Crear usuario y generar token JWT
        Usuario conductor = usuarioRepositoryPort.findByCorreo(WS_USER_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Conductor",
                "WS",
                WS_USER_EMAIL,
                "+50379990001",
                passwordEncoderPort.encode(COMMON_PASS),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CONDUCTOR, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        String token = jwtService.generateToken(conductor.id(), conductor.correo(), conductor.nombre(), "ACTIVO", List.of("CONDUCTOR", "USUARIO"));

        // 2. Crear vehículo y viaje para tener una sala de conversación real
        Vehiculo vehiculo = new Vehiculo(
            null,
            conductor.id(),
            "Mazda",
            "3",
            2022,
            "Azul",
            TipoVehiculo.SEDAN,
            "P" + (System.currentTimeMillis() % 100000),
            4,
            null,
            true,
            null,
            null
        );
        Vehiculo vehiculoGuardado = vehiculoRepositoryPort.save(vehiculo);

        com.carpooling.sv.v1.viaje.domain.value.Coordenada origen = new com.carpooling.sv.v1.viaje.domain.value.Coordenada(13.6989, -89.1914);
        com.carpooling.sv.v1.viaje.domain.value.Coordenada destino = new com.carpooling.sv.v1.viaje.domain.value.Coordenada(13.9782, -89.5638);

        Viaje viaje = new Viaje(
            null,
            conductor.id(),
            vehiculoGuardado.id(),
            null,
            "San Salvador",
            origen,
            "Santa Ana",
            destino,
            LocalDate.now().plusDays(1),
            LocalTime.of(9, 0),
            3,
            3,
            BigDecimal.valueOf(3.00),
            "Viaje para prueba WS",
            com.carpooling.sv.v1.viaje.domain.value.EstadoViaje.PUBLICADO,
            List.of(),
            null,
            null
        );
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(conductor.correo());
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Viaje viajeCreado = publicarViajeCase.publicarViaje(viaje, List.of());

        // 3. Crear conversación
        Conversacion conversacion = crearConversacionCase.obtenerOCrearConversacionPorViaje(viajeCreado.id());
        assertNotNull(conversacion.id());

        // 4. Conectar cliente STOMP pasando el token JWT en el frame CONNECT
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        String wsUrl = "ws://localhost:" + port + "/ws";
        StompSession session = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        assertTrue(session.isConnected(), "El cliente WebSocket STOMP debería estar conectado");

        // 5. Suscribirse a /topic/conversaciones/{conversacionId}
        CompletableFuture<MensajeResponse> messageFuture = new CompletableFuture<>();

        session.subscribe("/topic/conversaciones/" + conversacion.id(), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MensajeResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((MensajeResponse) payload);
            }
        });

        // Dar un breve instante para asentar la suscripción en el broker
        Thread.sleep(300);

        // 6. Enviar mensaje en la conversación
        enviarMensajeCase.enviarMensaje(conversacion.id(), "¡Hola tiempo real desde WebSocket!");

        // 7. Esperar a que llegue el mensaje por STOMP al suscriptor en tiempo real
        MensajeResponse recibido = messageFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(recibido);
        assertEquals(conversacion.id(), recibido.conversacionId());
        assertEquals("¡Hola tiempo real desde WebSocket!", recibido.contenido());
        assertEquals(conductor.id(), recibido.remitenteId());

        session.disconnect();
    }
}
