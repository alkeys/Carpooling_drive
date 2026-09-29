package com.carpooling.sv.v1;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.carpooling.sv.v1.auth.infra.adapter.in.web.dto.LoginRequest;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeEnviarRequest;
import com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto.ResponderSolicitudRequest;
import com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto.SolicitudCrearRequest;
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;
import com.carpooling.sv.v1.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;
import com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.ViajePublicarRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MensajeriaIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Autowired
    private VehiculoRepositoryPort vehiculoRepositoryPort;

    @Autowired
    private PasswordEncoderPort passwordEncoderPort;

    private MockMvc mockMvc;

    private static Cookie conductorCookie;
    private static Cookie pasajeroCookie;
    private static Cookie intrusoCookie;
    private static UUID viajeId;
    private static String conversacionId;

    private static final String CONDUCTOR_EMAIL = "conductor.chat@carpooling.sv";
    private static final String PASAJERO_EMAIL = "pasajero.chat@carpooling.sv";
    private static final String INTRUSO_EMAIL = "intruso.chat@carpooling.sv";
    private static final String COMMON_PASSWORD = "PasswordChat123";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @Order(1)
    void setupData() throws Exception {
        Usuario cond = usuarioRepositoryPort.findByCorreo(CONDUCTOR_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Conductor",
                "Chat",
                CONDUCTOR_EMAIL,
                "+50375551111",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CONDUCTOR, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        String placaUnica = "P" + (System.currentTimeMillis() % 100000);
        Vehiculo vehiculo = new Vehiculo(
            null,
            cond.id(),
            "Toyota",
            "Corolla",
            2021,
            "Rojo",
            TipoVehiculo.SEDAN,
            placaUnica,
            4,
            null,
            true,
            null,
            null
        );
        Vehiculo vehiculoGuardado = vehiculoRepositoryPort.save(vehiculo);

        usuarioRepositoryPort.findByCorreo(PASAJERO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Pasajero",
                "Chat",
                PASAJERO_EMAIL,
                "+50375552222",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        usuarioRepositoryPort.findByCorreo(INTRUSO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Intruso",
                "Chat",
                INTRUSO_EMAIL,
                "+50375553333",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        conductorCookie = login(CONDUCTOR_EMAIL);
        pasajeroCookie = login(PASAJERO_EMAIL);
        intrusoCookie = login(INTRUSO_EMAIL);

        // Conductor publica viaje
        ViajePublicarRequest pubReq = new ViajePublicarRequest(
            vehiculoGuardado.id(),
            null,
            "Santa Tecla",
            13.6769,
            -89.2797,
            "San Salvador Centro",
            13.6989,
            -89.1914,
            LocalDate.now().plusDays(1),
            LocalTime.of(7, 30),
            3,
            BigDecimal.valueOf(2.00),
            "Viaje laboral hacia San Salvador",
            List.of()
        );

        MvcResult pubRes = mockMvc.perform(post("/api/v1/viajes")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pubReq)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode json = objectMapper.readTree(pubRes.getResponse().getContentAsString());
        viajeId = UUID.fromString(json.get("id").asText());

        // Pasajero solicita cupo y Conductor acepta
        SolicitudCrearRequest solReq = new SolicitudCrearRequest(viajeId, null, null, 1, "Voy con ustedes");
        MvcResult solRes = mockMvc.perform(post("/api/v1/solicitudes-viaje")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solReq)))
            .andExpect(status().isCreated())
            .andReturn();

        String solId = objectMapper.readTree(solRes.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/v1/solicitudes-viaje/" + solId + "/responder")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ResponderSolicitudRequest(true))))
            .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void shouldAllowConductorToOpenTripChat() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v1/conversaciones/viaje/" + viajeId)
                .cookie(conductorCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.viajeId").value(viajeId.toString()))
            .andExpect(jsonPath("$.participantesIds", hasSize(greaterThanOrEqualTo(1))))
            .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        conversacionId = node.get("id").asText();
    }

    @Test
    @Order(3)
    void shouldAllowAcceptedPassengerToAccessChatAndPostMessage() throws Exception {
        MensajeEnviarRequest msgReq = new MensajeEnviarRequest("Hola conductor, ¿a qué hora sales exactamente?");

        mockMvc.perform(post("/api/v1/conversaciones/" + conversacionId + "/mensajes")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msgReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.conversacionId").value(conversacionId))
            .andExpect(jsonPath("$.contenido").value("Hola conductor, ¿a qué hora sales exactamente?"));
    }

    @Test
    @Order(4)
    void shouldAllowConductorToReplyInChat() throws Exception {
        MensajeEnviarRequest replyReq = new MensajeEnviarRequest("Hola, salgo a las 7:30 puntual desde el parque.");

        mockMvc.perform(post("/api/v1/conversaciones/" + conversacionId + "/mensajes")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.contenido").value("Hola, salgo a las 7:30 puntual desde el parque."));
    }

    @Test
    @Order(5)
    void shouldAllowParticipantsToFetchMessageHistory() throws Exception {
        mockMvc.perform(get("/api/v1/conversaciones/" + conversacionId + "/mensajes")
                .cookie(pasajeroCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].contenido").value("Hola conductor, ¿a qué hora sales exactamente?"))
            .andExpect(jsonPath("$[1].contenido").value("Hola, salgo a las 7:30 puntual desde el parque."));
    }

    @Test
    @Order(6)
    void shouldBlockIntruderFromViewingOrPostingToTripChat() throws Exception {
        // Intruso intentando ver chat
        mockMvc.perform(get("/api/v1/conversaciones/viaje/" + viajeId)
                .cookie(intrusoCookie))
            .andExpect(status().isForbidden());

        // Intruso intentando ver mensajes
        mockMvc.perform(get("/api/v1/conversaciones/" + conversacionId + "/mensajes")
                .cookie(intrusoCookie))
            .andExpect(status().isForbidden());

        // Intruso intentando enviar mensaje
        MensajeEnviarRequest badMsg = new MensajeEnviarRequest("Mensaje de spam no autorizado");
        mockMvc.perform(post("/api/v1/conversaciones/" + conversacionId + "/mensajes")
                .cookie(intrusoCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badMsg)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void shouldListUserConversations() throws Exception {
        mockMvc.perform(get("/api/v1/conversaciones/mis-conversaciones")
                .cookie(pasajeroCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].id").value(conversacionId));
    }

    private Cookie login(String email) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, COMMON_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        Cookie jwtCookie = result.getResponse().getCookie("access_token");
        if (jwtCookie != null) {
            return jwtCookie;
        }

        String authHeader = result.getResponse().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return new Cookie("access_token", authHeader.substring(7));
        }

        throw new IllegalStateException("No se pudo obtener el token JWT para " + email);
    }
}
