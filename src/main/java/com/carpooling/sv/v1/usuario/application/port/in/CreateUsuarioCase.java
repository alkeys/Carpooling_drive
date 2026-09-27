package com.carpooling.sv.v1.usuario.application.port.in;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface CreateUsuarioCase {
    Usuario createUsuario(Usuario usuario);
}
