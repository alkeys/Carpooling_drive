package com.carpooling.sv.v1.usuario.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.shared.exception.DuplicateResourceException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.usuario.application.port.in.ChangeRolesCase;
import com.carpooling.sv.v1.usuario.application.port.in.CreateUsuarioCase;
import com.carpooling.sv.v1.usuario.application.port.in.DeleteUsuarioCase;
import com.carpooling.sv.v1.usuario.application.port.in.GetAllUsuariosCase;
import com.carpooling.sv.v1.usuario.application.port.in.GetUsuarioByIdCase;
import com.carpooling.sv.v1.usuario.application.port.in.UpdateUsuarioCase;
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;

@Service
@Transactional
public class UsuarioService implements
        CreateUsuarioCase,
        GetAllUsuariosCase,
        GetUsuarioByIdCase,
        UpdateUsuarioCase,
        DeleteUsuarioCase,
        ChangeRolesCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public UsuarioService(UsuarioRepositoryPort usuarioRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public Usuario createUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (usuarioRepositoryPort.existsByCorreo(usuario.correo())) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el correo: " + usuario.correo());
        }

        if (usuario.telefono() != null && !usuario.telefono().isBlank() && usuarioRepositoryPort.existsByTelefono(usuario.telefono())) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el teléfono: " + usuario.telefono());
        }

        // Cifrado de la contraseña con BCrypt
        String contrasenaCifrada = passwordEncoderPort.encode(usuario.contrasena());
        Usuario usuarioConClaveCifrada = usuario.withContrasena(contrasenaCifrada);

        return usuarioRepositoryPort.save(usuarioConClaveCifrada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> getAllUsuarios() {
        return usuarioRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getUsuarioById(UUID id) {
        return usuarioRepositoryPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public Usuario updateUsuario(UUID id, Usuario usuarioActualizado) {
        Usuario existente = usuarioRepositoryPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        // Si se intenta cambiar el teléfono, verificar que no esté ocupado por otro usuario
        if (usuarioActualizado.telefono() != null
                && !usuarioActualizado.telefono().isBlank()
                && !usuarioActualizado.telefono().equals(existente.telefono())
                && usuarioRepositoryPort.existsByTelefono(usuarioActualizado.telefono())) {
            throw new DuplicateResourceException("El número de teléfono ya está en uso por otro usuario");
        }

        // Si se incluye una nueva contraseña, encriptarla; de lo contrario, conservar la actual
        String claveFinal = existente.contrasena();
        if (usuarioActualizado.contrasena() != null && !usuarioActualizado.contrasena().isBlank()
                && !usuarioActualizado.contrasena().equals(existente.contrasena())) {
            claveFinal = passwordEncoderPort.encode(usuarioActualizado.contrasena());
        }

        Usuario usuarioParaActualizar = existente
            .withUpdatedData(
                usuarioActualizado.nombre(),
                usuarioActualizado.apellido(),
                usuarioActualizado.telefono(),
                usuarioActualizado.fotourl(),
                usuarioActualizado.estado(),
                usuarioActualizado.roles()
            )
            .withContrasena(claveFinal);

        return usuarioRepositoryPort.update(usuarioParaActualizar);
    }

    @Override
    public Usuario changeRoles(UUID id, java.util.Set<RolUsuario> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Debe asignarse al menos un rol al usuario");
        }
        Usuario existente = usuarioRepositoryPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        Usuario usuarioConNuevosRoles = existente.withRoles(roles);
        return usuarioRepositoryPort.update(usuarioConNuevosRoles);
    }

    @Override
    public void deleteUsuario(UUID id) {
        if (!usuarioRepositoryPort.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepositoryPort.deleteById(id);
    }
}
