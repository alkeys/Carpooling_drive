package com.carpooling.sv.v1.usuario.application.port.in;

import java.util.UUID;

public interface DeleteUsuarioCase {
    void deleteUsuario(UUID id);
}
