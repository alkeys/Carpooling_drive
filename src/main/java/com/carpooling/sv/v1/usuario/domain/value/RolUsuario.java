package com.carpooling.sv.v1.usuario.domain.value;

public enum RolUsuario {
    ADMIN,
    CONDUCTOR,
    CLIENTE,
    USUARIO;

    public static RolUsuario fromString(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return USUARIO;
        }
        String clean = roleName.trim().toUpperCase();
        if (clean.startsWith("ROLE_")) {
            clean = clean.substring(5);
        }
        for (RolUsuario rol : values()) {
            if (rol.name().equalsIgnoreCase(clean)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol inválido: " + roleName);
    }
}
