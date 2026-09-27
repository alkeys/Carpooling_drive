package com.carpooling.sv.v1.auth.infra.adapter.out.security;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.auth.application.port.out.TokenPort;
import com.carpooling.sv.v1.shared.security.JwtService;
import com.carpooling.sv.v1.usuario.domain.Usuario;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final JwtService jwtService;

    public JwtTokenAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generateToken(Usuario usuario) {
        java.util.List<String> rolesList = usuario.roles() != null
            ? usuario.roles().stream().map(Enum::name).toList()
            : java.util.List.of("CLIENTE", "USUARIO");

        return jwtService.generateToken(
            usuario.id(),
            usuario.correo(),
            usuario.nombre(),
            usuario.estado() != null ? usuario.estado().getEstadoUsuario() : "ACTIVO",
            rolesList
        );
    }
}
