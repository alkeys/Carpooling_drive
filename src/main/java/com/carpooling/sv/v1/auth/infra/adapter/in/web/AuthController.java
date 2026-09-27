package com.carpooling.sv.v1.auth.infra.adapter.in.web;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.auth.application.port.in.LoginCase;
import com.carpooling.sv.v1.auth.application.port.in.UpdateCase;
import com.carpooling.sv.v1.auth.infra.adapter.in.web.dto.AuthResponse;
import com.carpooling.sv.v1.auth.infra.adapter.in.web.dto.LoginRequest;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.CookieService;
import com.carpooling.sv.v1.usuario.application.port.in.CreateUsuarioCase;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;
import com.carpooling.sv.v1.usuario.infra.adapter.in.web.dto.UsuarioRequest;
import com.carpooling.sv.v1.usuario.infra.adapter.in.web.dto.UsuarioResponse;
import com.carpooling.sv.v1.usuario.infra.adapter.in.web.dto.UsuarioUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión, cierre de sesión y verificación de sesión con JWT por Cookies")
public class AuthController {

    private final LoginCase loginCase;
    private final CreateUsuarioCase createUsuarioCase;
    private final UpdateCase updateCase;
    private final CookieService cookieService;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public AuthController(LoginCase loginCase,
                          CreateUsuarioCase createUsuarioCase,
                          CookieService cookieService,
                          UsuarioRepositoryPort usuarioRepositoryPort,
                          UpdateCase updateCase) {
        this.loginCase = loginCase;
        this.createUsuarioCase = createUsuarioCase;
        this.cookieService = cookieService;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.updateCase = updateCase;
    }

    @Operation(
        summary = "Iniciar sesión (Login)",
        description = "Autentica al usuario mediante correo y contraseña. Genera un token JWT y lo establece en una Cookie HttpOnly ('access_token') segura."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso. Se envía cookie HttpOnly en la cabecera Set-Cookie."),
        @ApiResponse(responseCode = "400", description = "Credenciales incompletas o inválidas"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta inactiva")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCase.LoginResult result = loginCase.login(request.getCorreo(), request.getContrasena());
        Usuario usuario = result.usuario();

        ResponseCookie jwtCookie = cookieService.createJwtCookie(result.token());

        java.util.Set<String> roles = usuario.roles() != null
            ? usuario.roles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
            : java.util.Set.of("CLIENTE", "USUARIO");

        AuthResponse authResponse = new AuthResponse(
            usuario.id().toString(),
            usuario.nombre(),
            usuario.apellido(),
            usuario.correo(),
            usuario.telefono(),
            usuario.fotourl(),
            usuario.estado().getEstadoUsuario(),
            roles,
            "Inicio de sesión exitoso. Cookie HttpOnly configurada.",
            result.token()
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(authResponse);
    }

    @Operation(
        summary = "Cerrar sesión (Logout)",
        description = "Invalida la sesión del usuario eliminando la Cookie HttpOnly ('access_token') del navegador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie cleanCookie = cookieService.createCleanJwtCookie();
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
            .body(Map.of("mensaje", "Sesión finalizada exitosamente. Cookie eliminada."));
    }

    @Operation(
        summary = "Obtener perfil del usuario autenticado (/me)",
        description = "Retorna la información del usuario autenticado a través de la Cookie o el Bearer Token actual."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil del usuario autenticado"),
        @ApiResponse(responseCode = "401", description = "No autenticado o token expirado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado en base de datos")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String correo = authentication.getName();
        Usuario usuario = usuarioRepositoryPort.findByCorreo(correo)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        java.util.Set<String> userRoles = usuario.roles() != null
            ? usuario.roles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
            : java.util.Set.of("CLIENTE", "USUARIO");

        UsuarioResponse response = new UsuarioResponse(
            usuario.id().toString(),
            usuario.nombre(),
            usuario.apellido(),
            usuario.correo(),
            usuario.telefono(),
            usuario.fotourl(),
            usuario.verificado(),
            usuario.estado().getEstadoUsuario(),
            userRoles,
            usuario.creadoEn().toString(),
            usuario.actualizadoEn().toString()
        );

        return ResponseEntity.ok(response);
    }



    @Operation(
        summary = "Actualizar información del usuario autenticado (/me/update)",
        description = "Permite al usuario autenticado actualizar su información personal. Requiere autenticación mediante Cookie o Bearer Token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información del usuario actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado o token expirado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado en base de datos")
    })
    @PostMapping("/me/update")
    public ResponseEntity<UsuarioResponse> updateAuthenticatedUser(@Valid @RequestBody UsuarioRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String correo = authentication.getName();
        Usuario usuario = usuarioRepositoryPort.findByCorreo(correo)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        //verificar que el id del usuario autenticado coincida con el id del usuario que se quiere actualizar
    
        UsuarioResponse response = new UsuarioResponse(
            usuario.id().toString(),
            updateRequest.getNombre() != null ? updateRequest.getNombre() : usuario.nombre(),
            updateRequest.getApellido() != null ? updateRequest.getApellido() : usuario.apellido(),
            usuario.correo(), // No se permite cambiar el correo
            updateRequest.getTelefono() != null ? updateRequest.getTelefono() : usuario.telefono(),
            updateRequest.getFotourl() != null ? updateRequest.getFotourl() : usuario.fotourl(),
            usuario.verificado(),
            usuario.estado().getEstadoUsuario(),
            usuario.roles() != null
                ? usuario.roles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
                : java.util.Set.of("CLIENTE", "USUARIO"),
            usuario.creadoEn().toString(),
            java.time.Instant.now().toString() // Actualizar la fecha de actualización
        );

        // Actualizar el usuario en la base de datos
        Usuario updatedUsuario=new Usuario(
            usuario.id(),
            response.nombre(),
            response.apellido(),
            usuario.correo(), // No se permite cambiar el correo
            response.telefono(),
            usuario.contrasena(), // No se permite cambiar la contraseña aquí
            response.fotourl(),
            usuario.verificado(),
            usuario.estado(),
            usuario.roles(),
            usuario.creadoEn(),
            java.time.OffsetDateTime.now() // Actualizar la fecha de actualización
        );;
        if (updateCase.updateUsuario(updatedUsuario)== null) {
            throw new ResourceNotFoundException("Error al actualizar el usuario con correo: " + correo);
        }
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Registrar o crear un usuario", description = "Endpoint público para el registro de nuevos usuarios en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El correo o teléfono ya están registrados")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse createUsuario(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        Set<RolUsuario> roles = usuarioRequest.getRoles() != null && !usuarioRequest.getRoles().isEmpty()
            ? usuarioRequest.getRoles().stream().map(RolUsuario::fromString).collect(Collectors.toSet())
            : Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO);

        Usuario newUsuario = new Usuario(
            null,
            usuarioRequest.getNombre(),
            usuarioRequest.getApellido(),
            usuarioRequest.getCorreo(),
            usuarioRequest.getTelefono(),
            usuarioRequest.getContrasena(),
            usuarioRequest.getFotourl(),
            false,
            EstadoUsuarios.ACTIVO,
            roles,
            null,
            null
        );

        Usuario createdUsuario = createUsuarioCase.createUsuario(newUsuario);

        return new UsuarioResponse(
            createdUsuario.id().toString(),
            createdUsuario.nombre(),
            createdUsuario.apellido(),
            createdUsuario.correo(),
            createdUsuario.telefono(),
            createdUsuario.fotourl(),
            createdUsuario.verificado(),
            createdUsuario.estado() != null ? createdUsuario.estado().getEstadoUsuario() : "ACTIVO",
            createdUsuario.roles() != null
                ? createdUsuario.roles().stream().map(Enum::name).collect(Collectors.toSet())
                : Set.of("CLIENTE", "USUARIO"),
            createdUsuario.creadoEn() != null ? createdUsuario.creadoEn().toString() : null,
            createdUsuario.actualizadoEn() != null ? createdUsuario.actualizadoEn().toString() : null
        );
    }
}
