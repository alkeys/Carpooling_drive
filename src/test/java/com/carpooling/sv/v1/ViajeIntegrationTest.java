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
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;
import com.carpooling.sv.v1.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;
import com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.PuntoEncuentroRequest;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.ViajePublicarRequest;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.ViajeUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ViajeIntegrationTest {

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
    private static UUID conductorId;
    private static UUID vehiculoConductorId;
    private static String createdViajeId;

    private static final String CONDUCTOR_EMAIL = "conductor.viaje@carpooling.sv";
    private static final String PASAJERO_EMAIL = "pasajero.viaje@carpooling.sv";
    private static final String COMMON_PASSWORD = "PasswordViaje123";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @Order(1)
    void setupUsuariosYVehiculos() throws Exception {
        // 1. Conductor
        Usuario cond = usuarioRepositoryPort.findByCorreo(CONDUCTOR_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Roberto",
                "Conductor",
                CONDUCTOR_EMAIL,
                "+50379998888",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CONDUCTOR, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });
        conductorId = cond.id();

        // Login conductor
        LoginRequest condLogin = new LoginRequest(CONDUCTOR_EMAIL, COMMON_PASSWORD);
        MvcResult condRes = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(condLogin)))
            .andExpect(status().isOk())
            .andReturn();
        conductorCookie = condRes.getResponse().getCookie("access_token");

        // 2. Pasajero
        usuarioRepositoryPort.findByCorreo(PASAJERO_EMAIL).orElseGet(() -> {
            Usuario u = new Usuario(
                "Elena",
                "Pasajera",
                PASAJERO_EMAIL,
                "+50376665555",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            return usuarioRepositoryPort.save(u);
        });

        LoginRequest pasLogin = new LoginRequest(PASAJERO_EMAIL, COMMON_PASSWORD);
        MvcResult pasRes = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pasLogin)))
            .andExpect(status().isOk())
            .andReturn();
        pasajeroCookie = pasRes.getResponse().getCookie("access_token");

        // 3. Vehículo del conductor
        String placaUnica = "P" + (System.currentTimeMillis() % 100000);
        Vehiculo vehiculo = new Vehiculo(
            null,
            conductorId,
            "Nissan",
            "Sentra",
            2022,
            "Plateado",
            TipoVehiculo.SEDAN,
            placaUnica,
            4,
            null,
            true,
            null,
            null
        );
        Vehiculo vGuardado = vehiculoRepositoryPort.save(vehiculo);
        vehiculoConductorId = vGuardado.id();
    }

    @Test
    @Order(2)
    void shouldAllowPublicSearchOfViajesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/viajes/buscar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @Order(3)
    void shouldRejectViajePublishByNonConductor() throws Exception {
        ViajePublicarRequest request = new ViajePublicarRequest(
            vehiculoConductorId,
            null,
            "Santa Ana",
            13.9782,
            -89.5638,
            "San Salvador",
            13.7013,
            -89.2244,
            LocalDate.now().plusDays(1),
            LocalTime.of(7, 0),
            3,
            new BigDecimal("3.00"),
            "Viaje de prueba",
            List.of()
        );

        mockMvc.perform(post("/api/v1/viajes")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void shouldAllowConductorToPublishViajeWithMeetingPoints() throws Exception {
        PuntoEncuentroRequest punto1 = new PuntoEncuentroRequest(
            "Gasolinera Puma El Congo",
            "Frente a pasarela",
            13.9056,
            -89.5023,
            1,
            true,
            true
        );

        ViajePublicarRequest request = new ViajePublicarRequest(
            vehiculoConductorId,
            null,
            "Metrocentro Santa Ana",
            13.9782,
            -89.5638,
            "Plaza Salvador del Mundo",
            13.7013,
            -89.2244,
            LocalDate.now().plusDays(2),
            LocalTime.of(6, 30),
            3,
            new BigDecimal("3.50"),
            "Salida puntual desde Santa Ana hacia San Salvador",
            List.of(punto1)
        );

        MvcResult result = mockMvc.perform(post("/api/v1/viajes")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.origenNombre").value("Metrocentro Santa Ana"))
            .andExpect(jsonPath("$.cuposTotales").value(3))
            .andExpect(jsonPath("$.cuposDisponibles").value(3))
            .andExpect(jsonPath("$.estado").value("PUBLICADO"))
            .andExpect(jsonPath("$.puntosEncuentro", hasSize(1)))
            .andExpect(jsonPath("$.puntosEncuentro[0].nombre").value("Gasolinera Puma El Congo"))
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        createdViajeId = json.get("id").asText();
    }

    @Test
    @Order(5)
    void shouldFindTripByPostGisSpatialCoordinates() throws Exception {
        // Búsqueda espacial cerca de Metrocentro Santa Ana (origen) y Salvador del Mundo (destino)
        mockMvc.perform(get("/api/v1/viajes/buscar")
                .param("origenLat", "13.9780")
                .param("origenLon", "-89.5630")
                .param("radioOrigenKm", "5.0")
                .param("destinoLat", "13.7010")
                .param("destinoLon", "-89.2240")
                .param("radioDestinoKm", "5.0")
                .param("cuposMinimos", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$[0].id").value(createdViajeId));
    }

    @Test
    @Order(6)
    void shouldReturnEmptyWhenSpatialCoordinatesDoNotMatch() throws Exception {
        // Coordenadas en otro país / muy lejanas
        mockMvc.perform(get("/api/v1/viajes/buscar")
                .param("origenLat", "14.6349") // Ciudad de Guatemala
                .param("origenLon", "-90.5069")
                .param("radioOrigenKm", "2.0")
                .param("destinoLat", "13.7010")
                .param("destinoLon", "-89.2240")
                .param("radioDestinoKm", "2.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(7)
    void shouldGetViajeByIdPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/viajes/" + createdViajeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdViajeId))
            .andExpect(jsonPath("$.origenLat").value(13.9782))
            .andExpect(jsonPath("$.origenLon").value(-89.5638));
    }

    @Test
    @Order(8)
    void shouldEnforceAntiIdorWhenAnotherUserTriesToUpdateViaje() throws Exception {
        ViajeUpdateRequest updateRequest = new ViajeUpdateRequest();
        updateRequest.setOrigenNombre("Origen Hackeado");

        mockMvc.perform(put("/api/v1/viajes/" + createdViajeId)
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void shouldAllowConductorToUpdateViaje() throws Exception {
        ViajeUpdateRequest updateRequest = new ViajeUpdateRequest();
        updateRequest.setTarifaAproximada(new BigDecimal("4.00"));
        updateRequest.setDescripcion("Tarifa actualizada con paradas confirmadas");

        mockMvc.perform(put("/api/v1/viajes/" + createdViajeId)
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tarifaAproximada").value(4.00))
            .andExpect(jsonPath("$.descripcion").value("Tarifa actualizada con paradas confirmadas"));
    }

    @Test
    @Order(10)
    void shouldAllowConductorToChangeViajeState() throws Exception {
        mockMvc.perform(patch("/api/v1/viajes/" + createdViajeId + "/estado")
                .cookie(conductorCookie)
                .param("estado", "EN_CURSO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        mockMvc.perform(patch("/api/v1/viajes/" + createdViajeId + "/estado")
                .cookie(conductorCookie)
                .param("estado", "FINALIZADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }
}
