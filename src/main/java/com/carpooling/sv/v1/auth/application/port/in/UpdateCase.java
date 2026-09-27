package com.carpooling.sv.v1.auth.application.port.in;



import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface UpdateCase {
    Usuario updateUsuario(Usuario usuario);
}
