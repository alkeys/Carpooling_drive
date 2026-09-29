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
import com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto.CalificarRequest;
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
class CalificacionIntegrationTest {

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
    private static UUID conductorId;
    private static UUID pasajeroId;
    private static UUID viajeId;

    private static final String CONDUCTOR_EMAIL = "conductor.review@carpooling.sv";
    private static final String PASAJERO_EMAIL = "pasajero.review@carpooling.sv";
    private static final String INTRUSO_EMAIL = "intruso.review@carpooling.sv";
    private static final String COMMON_PASSWORD = "PasswordReview123";

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
                "Review",
                CONDUCTOR_EMAIL,
                "+50374441111",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CONDUCTOR, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });
        conductorId = cond.id();

        String placaUnica = "P" + (System.currentTimeMillis() % 100000);
        Vehiculo vehiculo = new Vehiculo(
            null,
            conductorId,
            "Honda",
            "Civic",
            2023,
            "Negro",
            TipoVehiculo.SEDAN,
            placaUnica,
            4,
            null,
            true,
            null,
            null
        );
        Vehiculo vehiculoGuardado = vehiculoRepositoryPort.save(vehiculo);

        Usuario pas = usuarioRepositoryPort.findByCorreo(PASAJERO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Pasajero",
                "Review",
                PASAJERO_EMAIL,
                "+50374442222",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });
        pasajeroId = pas.id();

        usuarioRepositoryPort.findByCorreo(INTRUSO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Intruso",
                "Review",
                INTRUSO_EMAIL,
                "+50374443333",
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
            "Soyapango",
            13.7102,
            -89.1399,
            "Zona Rosa San Salvador",
            13.6931,
            -89.2415,
            LocalDate.now().plusDays(1),
            LocalTime.of(8, 0),
            3,
            BigDecimal.valueOf(2.50),
            "Viaje hacia Zona Rosa",
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
        SolicitudCrearRequest solReq = new SolicitudCrearRequest(viajeId, null, null, 1, "Viajo puntual");
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

        // Conductor pasa viaje a EN_CURSO y luego a FINALIZADO
        mockMvc.perform(patch("/api/v1/viajes/" + viajeId + "/estado?estado=EN_CURSO")
                .cookie(conductorCookie))
            .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/viajes/" + viajeId + "/estado?estado=FINALIZADO")
                .cookie(conductorCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    @Test
    @Order(2)
    void shouldAllowPassengerToRateConductor() throws Exception {
        CalificarRequest req = new CalificarRequest(
            viajeId,
            conductorId,
            5,
            "Excelente conductor, muy puntual y el auto impecable."
        );

        mockMvc.perform(post("/api/v1/calificaciones")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.viajeId").value(viajeId.toString()))
            .andExpect(jsonPath("$.evaluadorId").value(pasajeroId.toString()))
            .andExpect(jsonPath("$.evaluadoId").value(conductorId.toString()))
            .andExpect(jsonPath("$.puntuacion").value(5))
            .andExpect(jsonPath("$.comentario").value("Excelente conductor, muy puntual y el auto impecable."));
    }

    @Test
    @Order(3)
    void shouldAllowConductorToRatePassenger() throws Exception {
        CalificarRequest req = new CalificarRequest(
            viajeId,
            pasajeroId,
            5,
            "Excelente pasajera, muy respetuosa."
        );

        mockMvc.perform(post("/api/v1/calificaciones")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.puntuacion").value(5))
            .andExpect(jsonPath("$.comentario").value("Excelente pasajera, muy respetuosa."));
    }

    @Test
    @Order(4)
    void shouldRejectDuplicateRatingFromSameUserForSameTrip() throws Exception {
        CalificarRequest duplicate = new CalificarRequest(
            viajeId,
            conductorId,
            4,
            "Intentando calificar dos veces"
        );

        mockMvc.perform(post("/api/v1/calificaciones")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isConflict());
    }

    @Test
    @Order(5)
    void shouldRejectSelfRating() throws Exception {
        CalificarRequest selfRate = new CalificarRequest(
            viajeId,
            conductorId,
            5,
            "Autocalificación no permitida"
        );

        mockMvc.perform(post("/api/v1/calificaciones")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selfRate)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void shouldBlockIntruderFromRatingTripParticipants() throws Exception {
        CalificarRequest intruderReq = new CalificarRequest(
            viajeId,
            conductorId,
            1,
            "Intruso dejando mala reseña"
        );

        mockMvc.perform(post("/api/v1/calificaciones")
                .cookie(intrusoCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(intruderReq)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void shouldAllowPublicAccessToUserReputation() throws Exception {
        // Acceso completamente público sin ningún cookie o token JWT
        mockMvc.perform(get("/api/v1/calificaciones/usuario/" + conductorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuarioId").value(conductorId.toString()))
            .andExpect(jsonPath("$.promedioPuntuacion", notNullValue()))
            .andExpect(jsonPath("$.totalResenas", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ultimasCalificaciones", hasSize(greaterThanOrEqualTo(1))));
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
