package com.carpooling.sv.v1.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.JpaUsuario;
import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.value.EstadoUsuariosEnum;

public class SecurityUser implements UserDetails {

    private final UUID id;
    private final String nombre;
    private final String correo;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(JpaUsuario jpaUsuario) {
        this.id = jpaUsuario.getId();
        this.nombre = jpaUsuario.getNombre() + (jpaUsuario.getApellido() != null ? " " + jpaUsuario.getApellido() : "");
        this.correo = jpaUsuario.getCorreo();
        this.password = jpaUsuario.getPasswordHash();
        this.active = jpaUsuario.getEstado() == EstadoUsuariosEnum.ACTIVO;
        if (jpaUsuario.getRoles() != null && !jpaUsuario.getRoles().isEmpty()) {
            this.authorities = jpaUsuario.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .toList();
        } else {
            this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_CLIENTE"),
                new SimpleGrantedAuthority("ROLE_USUARIO")
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
