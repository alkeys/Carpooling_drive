package com.carpooling.sv.v1.usuario.application.port.in;

import java.util.List;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface GetAllUsuariosCase {
    // Define el método para obtener todos los usuarios
    List<Usuario> getAllUsuarios();
    
}
