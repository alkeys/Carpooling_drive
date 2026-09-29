package com.carpooling.sv.v1;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

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
import com.carpooling.sv.v1.ruta.infra.adapter.in.web.dto.RutaRequest;
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;
import com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto.VehiculoRequest;
import com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto.VehiculoUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VehiculoAndRutaIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Autowired
    private PasswordEncoderPort passwordEncoderPort;

    private MockMvc mockMvc;

    private static Cookie adminCookie;
    private static Cookie conductorCookie;
    private static Cookie pasajeroCookie;
    private static String createdVehiculoId;
    private static String createdRutaId;

    private static final String CONDUCTOR_EMAIL = "conductor.test@carpooling.sv";
    private static final String PASAJERO_EMAIL = "pasajero.test@carpooling.sv";
    private static final String COMMON_PASSWORD = "Password123";
    private static final String TEST_PLACA = "P" + (System.currentTimeMillis() % 100000);
    private static final String TEST_RUTA = "Ruta " + System.currentTimeMillis();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @Order(1)
    void setupUsersAndLogins() throws Exception {
        // 1. Login como ADMIN predeterminado
        LoginRequest adminLogin = new LoginRequest("admin@admin.com", "admin");
        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
            .andExpect(status().isOk())
            .andReturn();
        adminCookie = adminRes.getResponse().getCookie("access_token");

        // 2. Crear usuario Conductor
        if (usuarioRepositoryPort.findByCorreo(CONDUCTOR_EMAIL).isEmpty()) {
            Usuario conductor = new Usuario(
                "Carlos",
                "Conductor",
                CONDUCTOR_EMAIL,
                "+50371112222",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CONDUCTOR, RolUsuario.USUARIO)
            );
            usuarioRepositoryPort.save(conductor);
        }

        LoginRequest conductorLogin = new LoginRequest(CONDUCTOR_EMAIL, COMMON_PASSWORD);
        MvcResult condRes = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conductorLogin)))
            .andExpect(status().isOk())
            .andReturn();
        conductorCookie = condRes.getResponse().getCookie("access_token");

        // 3. Crear usuario Pasajero (Cliente)
        if (usuarioRepositoryPort.findByCorreo(PASAJERO_EMAIL).isEmpty()) {
            Usuario pasajero = new Usuario(
                "Maria",
                "Pasajera",
                PASAJERO_EMAIL,
                "+50373334444",
                passwordEncoderPort.encode(COMMON_PASSWORD),
                null,
                true,
                EstadoUsuarios.ACTIVO,
                Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
            );
            usuarioRepositoryPort.save(pasajero);
        }

        LoginRequest pasajeroLogin = new LoginRequest(PASAJERO_EMAIL, COMMON_PASSWORD);
        MvcResult pasRes = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pasajeroLogin)))
            .andExpect(status().isOk())
            .andReturn();
        pasajeroCookie = pasRes.getResponse().getCookie("access_token");
    }

    @Test
    @Order(2)
    void shouldAllowPublicBrowsingOfRutas() throws Exception {
        mockMvc.perform(get("/api/v1/rutas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @Order(3)
    void shouldRejectRouteCreationByNonAdmin() throws Exception {
        RutaRequest request = new RutaRequest("Santa Ana - Ahuachapan", "Por Chalchuapa");
        mockMvc.perform(post("/api/v1/rutas")
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void shouldAllowRouteCreationByAdmin() throws Exception {
        RutaRequest request = new RutaRequest(TEST_RUTA, "Vía Panamericana");
        MvcResult result = mockMvc.perform(post("/api/v1/rutas")
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.nombre").value(TEST_RUTA))
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        createdRutaId = json.get("id").asText();
    }

    @Test
    @Order(5)
    void shouldAllowConductorToRegisterVehiculo() throws Exception {
        VehiculoRequest request = new VehiculoRequest(
            "Toyota",
            "Corolla",
            2021,
            "Rojo",
            "SEDAN",
            TEST_PLACA,
            4,
            "https://imagen.com/corolla.jpg"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/vehiculos")
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.marca").value("Toyota"))
            .andExpect(jsonPath("$.placa").value(TEST_PLACA))
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        createdVehiculoId = json.get("id").asText();
    }

    @Test
    @Order(6)
    void shouldAllowConductorToListOwnVehiculos() throws Exception {
        mockMvc.perform(get("/api/v1/vehiculos/mis-vehiculos")
                .cookie(conductorCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$[0].placa", notNullValue()));
    }

    @Test
    @Order(7)
    void shouldEnforceAntiIdorWhenAnotherUserTriesToAccessVehiculo() throws Exception {
        // El pasajero intenta consultar o modificar el vehículo del conductor pasando el UUID
        mockMvc.perform(get("/api/v1/vehiculos/" + createdVehiculoId)
                .cookie(pasajeroCookie))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("permiso")));
    }

    @Test
    @Order(8)
    void shouldEnforceAntiIdorWhenAnotherUserTriesToUpdateVehiculo() throws Exception {
        VehiculoUpdateRequest updateRequest = new VehiculoUpdateRequest();
        updateRequest.setColor("Azul");

        mockMvc.perform(put("/api/v1/vehiculos/" + createdVehiculoId)
                .cookie(pasajeroCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void shouldAllowOwnerToUpdateVehiculo() throws Exception {
        VehiculoUpdateRequest updateRequest = new VehiculoUpdateRequest();
        updateRequest.setColor("Blanco Perla");

        mockMvc.perform(put("/api/v1/vehiculos/" + createdVehiculoId)
                .cookie(conductorCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.color").value("Blanco Perla"));
    }

    @Test
    @Order(10)
    void shouldAllowOwnerToSoftDeleteVehiculo() throws Exception {
        mockMvc.perform(delete("/api/v1/vehiculos/" + createdVehiculoId)
                .cookie(conductorCookie))
            .andExpect(status().isNoContent());

        // Comprobar que ya no aparece en activos
        mockMvc.perform(get("/api/v1/vehiculos/" + createdVehiculoId)
                .cookie(conductorCookie))
            .andExpect(status().isNotFound());
    }
}
