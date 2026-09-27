package com.carpooling.sv.v1.usuario.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface GetUsuarioByIdCase {
    Usuario getUsuarioById(UUID id);
}
