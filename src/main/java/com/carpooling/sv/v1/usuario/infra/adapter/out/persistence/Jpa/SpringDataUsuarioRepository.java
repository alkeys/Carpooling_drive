package com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.Jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.JpaUsuario;

@Repository 
public interface SpringDataUsuarioRepository extends JpaRepository<JpaUsuario, UUID> {
    Optional<JpaUsuario> findByCorreo(String correo);
    boolean existsById(UUID id);
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
}
