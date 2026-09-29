package com.carpooling.sv.v1.usuario.infra.adapter.in.web;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.usuario.application.port.in.ChangeRolesCase;
import com.carpooling.sv.v1.usuario.application.port.in.CreateUsuarioCase;
import com.carpooling.sv.v1.usuario.application.port.in.DeleteUsuarioCase;
import com.carpooling.sv.v1.usuario.application.port.in.GetAllUsuariosCase;
import com.carpooling.sv.v1.usuario.application.port.in.GetUsuarioByIdCase;
import com.carpooling.sv.v1.usuario.application.port.in.UpdateUsuarioCase;
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
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Operaciones CRUD para la gestión de usuarios y asignación de roles")
public class UsuarioController {

    private final CreateUsuarioCase createUsuarioCase;
    private final GetAllUsuariosCase getAllUsuariosCase;
    private final GetUsuarioByIdCase getUsuarioByIdCase;
    private final UpdateUsuarioCase updateUsuarioCase;
    private final DeleteUsuarioCase deleteUsuarioCase;
    private final ChangeRolesCase changeRolesCase;
    private final com.carpooling.sv.v1.shared.security.SecurityContextHelper securityHelper;

    public UsuarioController(
            CreateUsuarioCase createUsuarioCase,
            GetAllUsuariosCase getAllUsuariosCase,
            GetUsuarioByIdCase getUsuarioByIdCase,
            UpdateUsuarioCase updateUsuarioCase,
            DeleteUsuarioCase deleteUsuarioCase,
            ChangeRolesCase changeRolesCase,
            com.carpooling.sv.v1.shared.security.SecurityContextHelper securityHelper) {
        this.createUsuarioCase = createUsuarioCase;
        this.getAllUsuariosCase = getAllUsuariosCase;
        this.getUsuarioByIdCase = getUsuarioByIdCase;
        this.updateUsuarioCase = updateUsuarioCase;
        this.deleteUsuarioCase = deleteUsuarioCase;
        this.changeRolesCase = changeRolesCase;
        this.securityHelper = securityHelper;
    }

    @Operation(summary = "Registrar o crear un usuario", description = "Endpoint público para el registro de nuevos usuarios en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El correo o teléfono ya están registrados")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse createUsuario(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        Usuario usuario = toDomainUsuario(usuarioRequest);
        Usuario nuevoUsuario = createUsuarioCase.createUsuario(usuario);
        return toUsuarioResponse(nuevoUsuario);
    }

    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Obtiene los detalles del usuario actualmente logueado a través del token/sesión.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil recuperado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public UsuarioResponse getMiPerfil() {
        UUID currentUserId = securityHelper.getCurrentUserId();
        return toUsuarioResponse(getUsuarioByIdCase.getUsuarioById(currentUserId));
    }

    @Operation(summary = "Actualizar perfil propio", description = "Permite modificar datos personales (nombre, teléfono, foto) del usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/me")
    public UsuarioResponse updateMiPerfil(@Valid @RequestBody UsuarioUpdateRequest updateRequest) {
        UUID currentUserId = securityHelper.getCurrentUserId();
        Usuario usuarioActualizado = toDomainUsuario(currentUserId, updateRequest);
        return toUsuarioResponse(updateUsuarioCase.updateUsuario(currentUserId, usuarioActualizado));
    }

    @Operation(summary = "Listar todos los usuarios", description = "Obtiene la lista completa de usuarios registrados. Requiere autenticación.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public List<UsuarioResponse> getAllUsuarios() {
        return getAllUsuariosCase.getAllUsuarios().stream()
            .map(this::toUsuarioResponse)
            .toList();
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico mediante su UUID. Requiere autenticación.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{id}")
    public UsuarioResponse getUsuarioById(@PathVariable UUID id) {
        return toUsuarioResponse(getUsuarioByIdCase.getUsuarioById(id));
    }

    @Operation(summary = "Actualizar usuario", description = "Permite modificar datos de perfil de un usuario existente. Solo el propio usuario o ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No eres el propietario)"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con teléfono u otros datos únicos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}")
    public UsuarioResponse updateUsuario(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioUpdateRequest updateRequest) {
        UUID currentUserId = securityHelper.getCurrentUserId();
        if (!id.equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new com.carpooling.sv.v1.shared.exception.ForbiddenException("No tienes permiso para modificar los datos de otro usuario");
        }
        Usuario usuarioActualizado = toDomainUsuario(id, updateRequest);
        return toUsuarioResponse(updateUsuarioCase.updateUsuario(id, usuarioActualizado));
    }

    @Operation(summary = "Cambiar roles de un usuario", description = "Endpoint administrativo para asignar o modificar los roles de un usuario (ADMIN, CONDUCTOR, CLIENTE, USUARIO). Requiere rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Roles inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse changeRoles(
            @PathVariable UUID id,
            @RequestBody Set<String> roles) {
        Set<RolUsuario> domainRoles = roles.stream()
            .map(RolUsuario::fromString)
            .collect(Collectors.toSet());
        return toUsuarioResponse(changeRolesCase.changeRoles(id, domainRoles));
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID. Requiere rol ADMIN o ser el propio usuario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUsuario(@PathVariable UUID id) {
        UUID currentUserId = securityHelper.getCurrentUserId();
        if (!id.equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new com.carpooling.sv.v1.shared.exception.ForbiddenException("No tienes permiso para eliminar este usuario");
        }
        deleteUsuarioCase.deleteUsuario(id);
    }

    private Usuario toDomainUsuario(UsuarioRequest request) {
        Set<RolUsuario> roles = request.getRoles() != null && !request.getRoles().isEmpty()
            ? request.getRoles().stream().map(RolUsuario::fromString).collect(Collectors.toSet())
            : Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO);

        return new Usuario(
            null,
            request.getNombre(),
            request.getApellido(),
            request.getCorreo(),
            request.getTelefono(),
            request.getContrasena(),
            request.getFotourl(),
            false,
            EstadoUsuarios.ACTIVO,
            roles,
            null,
            null
        );
    }

    private Usuario toDomainUsuario(UUID id, UsuarioUpdateRequest request) {
        EstadoUsuarios estado = request.getEstado() != null && !request.getEstado().isBlank()
            ? new EstadoUsuarios(request.getEstado())
            : null;

        Set<RolUsuario> roles = request.getRoles() != null && !request.getRoles().isEmpty()
            ? request.getRoles().stream().map(RolUsuario::fromString).collect(Collectors.toSet())
            : null;

        return new Usuario(
            id,
            request.getNombre() != null ? request.getNombre() : "temp",
            request.getApellido(),
            "temp@temp.com", // el correo se conserva del usuario original en el servicio
            request.getTelefono(),
            request.getContrasena() != null ? request.getContrasena() : "tempPassword",
            request.getFotourl(),
            false,
            estado != null ? estado : EstadoUsuarios.ACTIVO,
            roles,
            null,
            null
        );
    }

    private UsuarioResponse toUsuarioResponse(Usuario usuario) {
        Set<String> roles = usuario.roles() != null
            ? usuario.roles().stream().map(Enum::name).collect(Collectors.toSet())
            : Set.of("CLIENTE", "USUARIO");

        return new UsuarioResponse(
            usuario.id() != null ? usuario.id().toString() : null,
            usuario.nombre(),
            usuario.apellido(),
            usuario.correo(),
            usuario.telefono(),
            usuario.fotourl(),
            usuario.verificado(),
            usuario.estado() != null ? usuario.estado().getEstadoUsuario() : "ACTIVO",
            roles,
            usuario.creadoEn() != null ? usuario.creadoEn().toString() : null,
            usuario.actualizadoEn() != null ? usuario.actualizadoEn().toString() : null
        );
    }
}
