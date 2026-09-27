package com.carpooling.sv.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;

import jakarta.servlet.http.Cookie;

@SpringBootTest
class AdminDataInitializerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldSeedDefaultAdminUserAndAllowLoginWithAdminAdmin() throws Exception {
        // 1. Verificar que el usuario admin@admin.com existe en el repositorio
        Optional<Usuario> adminOpt = usuarioRepositoryPort.findByCorreo("admin@admin.com");

        assertThat(adminOpt).isPresent();
        Usuario admin = adminOpt.get();
        assertThat(admin.hasRole(RolUsuario.ADMIN)).isTrue();
        assertThat(admin.correo()).isEqualTo("admin@admin.com");

        // 2. Iniciar sesión usando correo="admin@admin.com" y contrasena="admin"
        String loginJson = """
            {
                "correo": "admin@admin.com",
                "contrasena": "admin"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.roles", notNullValue()))
            .andExpect(jsonPath("$.correo").value("admin@admin.com"))
            .andReturn();

        Cookie adminCookie = result.getResponse().getCookie("access_token");
        assertThat(adminCookie).isNotNull();

        // 3. Probar también usando los alias {"email": "admin@admin.com", "password": "admin"}
        String loginAliasJson = """
            {
                "email": "admin@admin.com",
                "password": "admin"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginAliasJson))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE));

        // 4. Verificar que ingresar solo "admin" sin dominio es rechazado con 401 Unauthorized
        String invalidLoginJson = """
            {
                "correo": "admin",
                "contrasena": "admin"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginJson))
            .andExpect(status().isUnauthorized());

        // 5. Verificar acceso al perfil /me con la cookie del admin
        mockMvc.perform(get("/api/v1/auth/me")
                .cookie(adminCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Admin"))
            .andExpect(jsonPath("$.correo").value("admin@admin.com"))
            .andExpect(jsonPath("$.roles", notNullValue()));
    }
}
