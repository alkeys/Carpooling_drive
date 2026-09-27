package com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.Adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.Jpa.SpringDataUsuarioRepository;
import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.JpaUsuario;
import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.value.EstadoUsuariosEnum;
import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.value.RolUsuarioEnum;

@Component
public class JpaUsuarioAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;

    public JpaUsuarioAdapter(SpringDataUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        JpaUsuario jpaUsuario = toJpaUsuario(usuario);
        if (jpaUsuario.getEstado() == null) {
            jpaUsuario.setEstado(EstadoUsuariosEnum.ACTIVO);
        }
        if (jpaUsuario.getCreadoEn() == null) {
            jpaUsuario.setCreadoEn(OffsetDateTime.now());
        }
        if (jpaUsuario.getActualizadoEn() == null) {
            jpaUsuario.setActualizadoEn(OffsetDateTime.now());
        }
        JpaUsuario savedJpaUsuario = repository.save(jpaUsuario);
        return toDomainUsuario(savedJpaUsuario);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return repository.findById(id).map(this::toDomainUsuario);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return repository.findByCorreo(correo).map(this::toDomainUsuario);
    }

    @Override
    public List<Usuario> findAll() {
        return repository.findAll().stream()
            .map(this::toDomainUsuario)
            .toList();
    }

    @Override
    public Usuario update(Usuario usuario) {
        if (usuario.id() == null) {
            throw new IllegalArgumentException("El usuario debe tener un ID para ser actualizado");
        }
        if (!repository.existsById(usuario.id())) {
            throw new IllegalArgumentException("El usuario con ID " + usuario.id() + " no existe");
        }
        JpaUsuario jpaUsuario = toJpaUsuario(usuario);
        jpaUsuario.setActualizadoEn(OffsetDateTime.now());
        JpaUsuario updatedJpaUsuario = repository.save(jpaUsuario);
        return toDomainUsuario(updatedJpaUsuario);
    }

    @Override
    public void delete(Usuario usuario) {
        if (usuario != null && usuario.id() != null) {
            repository.deleteById(usuario.id());
        }
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return repository.existsByCorreo(correo);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return false;
        }
        return repository.existsByTelefono(telefono);
    }

    private JpaUsuario toJpaUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        EstadoUsuariosEnum estado = usuario.estado() != null
            ? EstadoUsuariosEnum.valueOf(usuario.estado().getEstadoUsuario())
            : EstadoUsuariosEnum.ACTIVO;

        java.util.Set<RolUsuarioEnum> roles = usuario.roles() != null
            ? usuario.roles().stream().map(r -> RolUsuarioEnum.valueOf(r.name())).collect(java.util.stream.Collectors.toSet())
            : java.util.Set.of(RolUsuarioEnum.CLIENTE, RolUsuarioEnum.USUARIO);

        return new JpaUsuario(
            usuario.id(),
            usuario.nombre(),
            usuario.apellido(),
            usuario.correo(),
            usuario.telefono(),
            usuario.contrasena(),
            usuario.fotourl(),
            usuario.verificado(),
            estado,
            roles,
            usuario.creadoEn() != null ? usuario.creadoEn() : OffsetDateTime.now(),
            usuario.actualizadoEn() != null ? usuario.actualizadoEn() : OffsetDateTime.now()
        );
    }

    private Usuario toDomainUsuario(JpaUsuario jpaUsuario) {
        if (jpaUsuario == null) {
            return null;
        }

        java.util.Set<com.carpooling.sv.v1.usuario.domain.value.RolUsuario> roles = jpaUsuario.getRoles() != null && !jpaUsuario.getRoles().isEmpty()
            ? jpaUsuario.getRoles().stream().map(r -> com.carpooling.sv.v1.usuario.domain.value.RolUsuario.valueOf(r.name())).collect(java.util.stream.Collectors.toSet())
            : java.util.Set.of(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CLIENTE, com.carpooling.sv.v1.usuario.domain.value.RolUsuario.USUARIO);

        return new Usuario(
            jpaUsuario.getId(),
            jpaUsuario.getNombre(),
            jpaUsuario.getApellido(),
            jpaUsuario.getCorreo(),
            jpaUsuario.getTelefono(),
            jpaUsuario.getPasswordHash(),
            jpaUsuario.getFotoUrl(),
            jpaUsuario.isVerificado(),
            jpaUsuario.getEstado() != null ? new EstadoUsuarios(jpaUsuario.getEstado().name()) : EstadoUsuarios.ACTIVO,
            roles,
            jpaUsuario.getCreadoEn(),
            jpaUsuario.getActualizadoEn()
        );
    }
}
