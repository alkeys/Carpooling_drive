package com.carpooling.sv.v1.usuario.application.port.in;

import java.util.Set;
import java.util.UUID;

import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;

public interface ChangeRolesCase {
    Usuario changeRoles(UUID id, Set<RolUsuario> roles);
}
