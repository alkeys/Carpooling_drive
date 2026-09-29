package com.carpooling.sv.v1;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class SolicitudIntegrationTest {

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
    private static String solicitudId;

    private static final String CONDUCTOR_EMAIL = "conductor.solicitud@carpooling.sv";
    private static final String PASAJERO_EMAIL = "pasajero.solicitud@carpooling.sv";
    private static final String INTRUSO_EMAIL = "intruso.solicitud@carpooling.sv";
    private static final String COMMON_PASSWORD = "PasswordSolicitud123";

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
        // 1. Crear Conductor
        Usuario cond = usuarioRepositoryPort.findByCorreo(CONDUCTOR_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Conductor",
                "Solicitudes",
                CONDUCTOR_EMAIL,
                "+50378881111",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CONDUCTOR, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        // Vehículo del conductor
        String placaUnica = "P" + (System.currentTimeMillis() % 100000);
        Vehiculo vehiculo = new Vehiculo(
            null,
            cond.id(),
            "Nissan",
            "Versa",
            2022,
            "Blanco",
            TipoVehiculo.SEDAN,
            placaUnica,
            4,
            null,
            true,
            null,
            null
        );
        Vehiculo vehiculoGuardado = vehiculoRepositoryPort.save(vehiculo);

        // 2. Crear Pasajero
        usuarioRepositoryPort.findByCorreo(PASAJERO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Pasajero",
                "Solicitante",
                PASAJERO_EMAIL,
                "+50378882222",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        // 3. Crear Intruso
        usuarioRepositoryPort.findByCorreo(INTRUSO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Intruso",
                "Desconocido",
                INTRUSO_EMAIL,
                "+50378883333",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        // Login conductor
        conductorCookie = login(CONDUCTOR_EMAIL);
        // Login pasajero
        pasajeroCookie = login(PASAJERO_EMAIL);
        // Login intruso
        intrusoCookie = login(INTRUSO_EMAIL);

        // Conductor publica un viaje con 3 cupos
        ViajePublicarRequest pubReq = new ViajePublicarRequest(
            vehiculoGuardado.id(),
            null,
            "Metrocentro San Salvador",
            13.7013,
            -89.2244,
            "Universidad de El Salvador",
            13.7160,
            -89.2025,
            LocalDate.now().plusDays(2),
            LocalTime.of(8, 0),
            3,
            BigDecimal.valueOf(2.50),
            "Viaje hacia la universidad con cupos",
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
    }

    @Test
    @Order(2)
    void shouldAllowPassengerToRequestSeatInTrip() throws Exception {
        SolicitudCrearRequest req = new SolicitudCrearRequest(
            viajeId,
            null,
            null,
            1,
            "Hola, me gustaría unirme al viaje."
        );

        MvcResult res = mockMvc.perform(post("/api/v1/solicitudes-viaje")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.viajeId").value(viajeId.toString()))
            .andExpect(jsonPath("$.cantidadCupos").value(1))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        solicitudId = node.get("id").asText();
    }

    @Test
    @Order(3)
    void shouldRejectConductorRequestingSeatInOwnTrip() throws Exception {
        SolicitudCrearRequest req = new SolicitudCrearRequest(
            viajeId,
            null,
            null,
            1,
            "Intentando unirme a mi propio viaje"
        );

        mockMvc.perform(post("/api/v1/solicitudes-viaje")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void shouldRejectDuplicateRequestWhenPending() throws Exception {
        SolicitudCrearRequest req = new SolicitudCrearRequest(
            viajeId,
            null,
            null,
            1,
            "Segunda solicitud duplicada"
        );

        mockMvc.perform(post("/api/v1/solicitudes-viaje")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test
    @Order(5)
    void shouldAllowConductorToViewTripSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-viaje/viaje/" + viajeId)
                .cookie(conductorCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @Order(6)
    void shouldBlockIntruderFromViewingTripSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-viaje/viaje/" + viajeId)
                .cookie(intrusoCookie))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void shouldAllowPassengerToViewOwnSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-viaje/mis-solicitudes")
                .cookie(pasajeroCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].id").value(solicitudId));
    }

    @Test
    @Order(8)
    void shouldAllowConductorToAcceptSolicitudAndDecrementAvailableSeats() throws Exception {
        ResponderSolicitudRequest respReq = new ResponderSolicitudRequest(true);

        mockMvc.perform(patch("/api/v1/solicitudes-viaje/" + solicitudId + "/responder")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(respReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ACEPTADA"))
            .andExpect(jsonPath("$.respondidoEn", notNullValue()));

        // Verificar que los cupos disponibles en el viaje bajaron de 3 a 2
        mockMvc.perform(get("/api/v1/viajes/" + viajeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cuposDisponibles").value(2));
    }

    @Test
    @Order(9)
    void shouldBlockIntruderFromCancellingPassengerSolicitud() throws Exception {
        mockMvc.perform(put("/api/v1/solicitudes-viaje/" + solicitudId + "/cancelar")
                .cookie(intrusoCookie))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(10)
    void shouldAllowPassengerToCancelAcceptedSolicitudAndRestoreAvailableSeats() throws Exception {
        mockMvc.perform(put("/api/v1/solicitudes-viaje/" + solicitudId + "/cancelar")
                .cookie(pasajeroCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADA"));

        // Verificar que los cupos disponibles en el viaje se restauraron de nuevo a 3
        mockMvc.perform(get("/api/v1/viajes/" + viajeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cuposDisponibles").value(3));
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
