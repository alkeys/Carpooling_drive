package com.carpooling.sv.v1.usuario.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.carpooling.sv.v1.usuario.domain.Usuario;

public interface UsuarioRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(UUID id);
    Optional<Usuario> findByCorreo(String correo);
    List<Usuario> findAll();
    Usuario update(Usuario usuario);
    void delete(Usuario usuario);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
}