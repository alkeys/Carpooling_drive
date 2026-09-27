package com.carpooling.sv.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.carpooling.sv.v1.shared.security.SecurityEndpoints;
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;

import jakarta.servlet.http.Cookie;

@SpringBootTest
class SecurityEndpointsAndRolesIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Autowired
    private PasswordEncoderPort passwordEncoderPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private Cookie loginAndGetCookie(String email, String password) throws Exception {
        String loginJson = """
            {
                "correo": "%s",
                "contrasena": "%s"
            }
            """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andReturn();

        return result.getResponse().getCookie("access_token");
    }

    @Test
    void shouldDenyAccessToBlockedEndpointsFromSecurityEndpointsClass() throws Exception {
        String testUserEmail = "test.blocked@rutea.sv";
        if (usuarioRepositoryPort.findByCorreo(testUserEmail).isEmpty()) {
            Usuario u = new Usuario(
                "Blocked", "Tester", testUserEmail, "+50379995566",
                passwordEncoderPort.encode("Clave123"), null, true,
                EstadoUsuarios.ACTIVO, Set.of(RolUsuario.ADMIN, RolUsuario.USUARIO)
            );
            usuarioRepositoryPort.save(u);
        }
        Cookie userCookie = loginAndGetCookie(testUserEmail, "Clave123");

        // Verifica que cualquier endpoint listado en SecurityEndpoints.BLOCKED_URLS sea denegado
        // tanto a usuarios autenticados (incluso con ADMIN) con 403 Forbidden
        // como a peticiones anónimas con 401/403
        for (String blockedPattern : SecurityEndpoints.BLOCKED_URLS) {
            String testUrl = blockedPattern.replace("/**", "/test");
            
            // Usuario autenticado (ADMIN) es rechazado con 403
            mockMvc.perform(get(testUrl).cookie(userCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

            // Petición anónima es rechazada (401 o 403)
            mockMvc.perform(get(testUrl))
                .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void shouldEnforceAdminRoleRestrictions() throws Exception {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String clienteEmail = "cliente." + randomSuffix + "@rutea.sv";
        Usuario cliente = new Usuario(
            "Cliente", "Prueba", clienteEmail, "+5037" + (int)(Math.random() * 9000000 + 1000000),
            passwordEncoderPort.encode("Clave123"), null, true,
            EstadoUsuarios.ACTIVO, Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO)
        );
        Usuario clienteGuardado = usuarioRepositoryPort.save(cliente);

        String adminEmail = "admin." + randomSuffix + "@rutea.sv";
        Usuario admin = new Usuario(
            "Admin", "Sistema", adminEmail, "+5037" + (int)(Math.random() * 9000000 + 1000000),
            passwordEncoderPort.encode("AdminClave123"), null, true,
            EstadoUsuarios.ACTIVO, Set.of(RolUsuario.ADMIN, RolUsuario.USUARIO)
        );
        usuarioRepositoryPort.save(admin);

        Cookie clienteCookie = loginAndGetCookie(clienteEmail, "Clave123");
        Cookie adminCookie = loginAndGetCookie(adminEmail, "AdminClave123");

        // 3. El usuario CLIENTE intenta cambiar roles (debe dar 403 Forbidden)
        String newRolesJson = "[\"ADMIN\", \"CONDUCTOR\"]";
        mockMvc.perform(put("/api/v1/usuarios/" + clienteGuardado.id() + "/roles")
                .cookie(clienteCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newRolesJson))
            .andExpect(status().isForbidden());

        // 4. El usuario ADMIN cambia roles del cliente (debe permitirlo con 200 OK)
        mockMvc.perform(put("/api/v1/usuarios/" + clienteGuardado.id() + "/roles")
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newRolesJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles").isArray());

        // 5. Verificar que el cliente ahora tiene los roles actualizados
        Usuario usuarioActualizado = usuarioRepositoryPort.findById(clienteGuardado.id()).orElseThrow();
        assertThat(usuarioActualizado.roles()).contains(RolUsuario.ADMIN, RolUsuario.CONDUCTOR);
    }
}
