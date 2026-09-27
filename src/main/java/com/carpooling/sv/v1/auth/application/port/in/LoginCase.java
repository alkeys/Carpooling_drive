package com.carpooling.sv.v1.auth.application.port.in;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface LoginCase {
    record LoginResult(Usuario usuario, String token) {}

    LoginResult login(String correo, String contrasena);
}
