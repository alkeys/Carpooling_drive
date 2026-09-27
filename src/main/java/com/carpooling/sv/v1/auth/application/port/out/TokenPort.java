package com.carpooling.sv.v1.auth.application.port.out;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface TokenPort {
    String generateToken(Usuario usuario);
}
