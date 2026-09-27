package com.carpooling.sv.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioAndAuthIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort usuarioRepositoryPort;

    private MockMvc mockMvc;

    private static String createdUserId;
    private static Cookie authCookie;
    private static final String TEST_EMAIL = "alex.tester@carpooling.sv";
    private static final String TEST_PASSWORD = "PasswordSegura123";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @Order(1)
    void shouldRegisterNewUsuario() throws Exception {
        usuarioRepositoryPort.findByCorreo(TEST_EMAIL).ifPresent(usuarioRepositoryPort::delete);

        String requestJson = """
            {
                "nombre": "Alex",
                "apellido": "Gómez",
                "correo": "%s",
                "telefono": "+50370001122",
                "fotourl": "https://example.com/alex.jpg",
                "contrasena": "%s"
            }
            """.formatted(TEST_EMAIL, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.correo").value(TEST_EMAIL))
            .andExpect(jsonPath("$.nombre").value("Alex"))
            .andExpect(jsonPath("$.apellido").value("Gómez"))
            .andExpect(jsonPath("$.estado").value("ACTIVO"))
            .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        createdUserId = responseNode.get("id").asText();
        assertThat(createdUserId).isNotNull();
    }

    @Test
    @Order(2)
    void shouldRejectDuplicateCorreo() throws Exception {
        String duplicateJson = """
            {
                "nombre": "Otro",
                "apellido": "Usuario",
                "correo": "%s",
                "telefono": "+50379998877",
                "contrasena": "OtraPassword123"
            }
            """.formatted(TEST_EMAIL);

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateJson))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("registrado con el correo")));
    }

    @Test
    @Order(3)
    void shouldLoginAndReceiveHttpOnlyCookie() throws Exception {
        String loginJson = """
            {
                "correo": "%s",
                "contrasena": "%s"
            }
            """.formatted(TEST_EMAIL, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
            .andExpect(jsonPath("$.correo").value(TEST_EMAIL))
            .andExpect(jsonPath("$.token", notNullValue()))
            .andReturn();

        authCookie = result.getResponse().getCookie("access_token");
        assertThat(authCookie).isNotNull();
        assertThat(authCookie.isHttpOnly()).isTrue();
    }

    @Test
    @Order(4)
    void shouldRejectAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    void shouldAllowAccessToGetAllUsuariosWithCookie() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                .cookie(authCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @Order(6)
    void shouldGetUsuarioByIdWithCookie() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/" + createdUserId)
                .cookie(authCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdUserId))
            .andExpect(jsonPath("$.correo").value(TEST_EMAIL));
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/usuarios/" + nonExistentId)
                .cookie(authCookie))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("Usuario no encontrado")));
    }

    @Test
    @Order(8)
    void shouldGetCurrentUserProfileWithAuthMe() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                .cookie(authCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdUserId))
            .andExpect(jsonPath("$.correo").value(TEST_EMAIL))
            .andExpect(jsonPath("$.nombre").value("Alex"));
    }

    @Test
    @Order(9)
    void shouldUpdateUsuarioWithCookie() throws Exception {
        String updateJson = """
            {
                "nombre": "Alexander",
                "apellido": "Gómez Rivas",
                "telefono": "+50370009999",
                "fotourl": "https://example.com/alex_updated.jpg",
                "estado": "ACTIVO"
            }
            """;

        mockMvc.perform(put("/api/v1/usuarios/" + createdUserId)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Alexander"))
            .andExpect(jsonPath("$.apellido").value("Gómez Rivas"))
            .andExpect(jsonPath("$.telefono").value("+50370009999"));
    }

    @Test
    @Order(10)
    void shouldLogoutAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .cookie(authCookie))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    @Order(11)
    void shouldDeleteUsuarioWithCookie() throws Exception {
        // Se elimina el usuario correctamente
        mockMvc.perform(delete("/api/v1/usuarios/" + createdUserId)
                .cookie(authCookie))
            .andExpect(status().isNoContent());

        // Luego de eliminado, cualquier petición con esa sesión es rechazada con 401
        mockMvc.perform(get("/api/v1/usuarios/" + createdUserId)
                .cookie(authCookie))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(12)
    void shouldRegisterNewUsuarioViaAuthRegisterAndAllowLogin() throws Exception {
        String authRegisterEmail = "auth.register." + UUID.randomUUID().toString().substring(0, 8) + "@rutea.sv";
        String authRegisterPassword = "PasswordAuth123";

        String authRegisterPhone = "+5037" + (int)(Math.random() * 9000000 + 1000000);

        String registerJson = """
            {
                "nombre": "Carlos",
                "apellido": "López",
                "correo": "%s",
                "telefono": "%s",
                "fotourl": "https://example.com/carlos.jpg",
                "contrasena": "%s"
            }
            """.formatted(authRegisterEmail, authRegisterPhone, authRegisterPassword);

        // 1. Registro público mediante /api/v1/auth/register (sin autenticación previa)
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.correo").value(authRegisterEmail))
            .andExpect(jsonPath("$.nombre").value("Carlos"))
            .andExpect(jsonPath("$.apellido").value("López"))
            .andExpect(jsonPath("$.estado").value("ACTIVO"));

        // 2. Iniciar sesión con el usuario recién registrado
        String loginJson = """
            {
                "correo": "%s",
                "contrasena": "%s"
            }
            """.formatted(authRegisterEmail, authRegisterPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.correo").value(authRegisterEmail));

        // 3. Intento de registro duplicado con el mismo correo debe responder 409 Conflict
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(status().isConflict());
    }
}
