package com.carpooling.sv.v1.auth.application.service;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.carpooling.sv.v1.auth.application.port.in.LoginCase;
import com.carpooling.sv.v1.auth.application.port.in.UpdateCase;
import com.carpooling.sv.v1.auth.application.port.out.TokenPort;
import com.carpooling.sv.v1.shared.exception.UnauthorizedException;
import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;

@Service
public class AuthService implements LoginCase, UpdateCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenPort tokenPort;

    public AuthService(UsuarioRepositoryPort usuarioRepositoryPort, PasswordEncoderPort passwordEncoderPort, TokenPort tokenPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenPort = tokenPort;
    }

    @Override
    public LoginResult login(String correo, String contrasena) {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new BadCredentialsException("El correo/usuario y la contraseña son obligatorios");
        }

        String inputIdentificador = correo.trim().toLowerCase();

        Usuario usuario = usuarioRepositoryPort.findByCorreo(inputIdentificador)
            .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoderPort.matches(contrasena, usuario.contrasena())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (EstadoUsuarios.SUSPENDIDO.equals(usuario.estado()) || EstadoUsuarios.INACTIVO.equals(usuario.estado())) {
            throw new UnauthorizedException("La cuenta de usuario no está activa (" + usuario.estado().getEstadoUsuario() + ")");
        }

        String token = tokenPort.generateToken(usuario);
        return new LoginResult(usuario, token);
    }

        @Override
        public Usuario updateUsuario(Usuario usuario) {
            return usuarioRepositoryPort.update(usuario);
        }
}
