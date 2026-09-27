package com.carpooling.sv.v1.usuario.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface UpdateUsuarioCase {
    Usuario updateUsuario(UUID id, Usuario usuarioActualizado);
}
