package com.carpooling.sv.v1.usuario.domain;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;

public record Usuario(
    UUID id,
    String nombre,
    String apellido,
    String correo,
    String telefono,
    String contrasena,
    String fotourl,
    boolean verificado,
    EstadoUsuarios estado,
    Set<RolUsuario> roles,
    OffsetDateTime creadoEn,
    OffsetDateTime actualizadoEn
) {
    public Usuario {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario no puede ser nulo o vacío");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo del usuario no puede ser nulo o vacío");
        }
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña del usuario no puede ser nula o vacía");
        }
        if (estado == null) {
            estado = EstadoUsuarios.ACTIVO;
        }
        if (roles == null || roles.isEmpty()) {
            roles = Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO);
        } else {
            roles = Collections.unmodifiableSet(new HashSet<>(roles));
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (actualizadoEn == null) {
            actualizadoEn = OffsetDateTime.now();
        }
    }

    public Usuario(String nombre, String apellido, String correo, String telefono, String contrasena, String fotourl, boolean verificado, EstadoUsuarios estado, Set<RolUsuario> roles) {
        this(null, nombre, apellido, correo, telefono, contrasena, fotourl, verificado, estado, roles, OffsetDateTime.now(), OffsetDateTime.now());
    }

    public Usuario(String nombre, String apellido, String correo, String telefono, String contrasena, String fotourl, boolean verificado, EstadoUsuarios estado) {
        this(null, nombre, apellido, correo, telefono, contrasena, fotourl, verificado, estado, Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO), OffsetDateTime.now(), OffsetDateTime.now());
    }

    public Usuario(String nombre, String apellido, String correo, String telefono, String contrasena, String fotourl) {
        this(null, nombre, apellido, correo, telefono, contrasena, fotourl, false, EstadoUsuarios.ACTIVO, Set.of(RolUsuario.CLIENTE, RolUsuario.USUARIO), OffsetDateTime.now(), OffsetDateTime.now());
    }

    public Usuario withId(UUID nuevoId) {
        return new Usuario(nuevoId, this.nombre, this.apellido, this.correo, this.telefono, this.contrasena, this.fotourl, this.verificado, this.estado, this.roles, this.creadoEn, this.actualizadoEn);
    }

    public Usuario withContrasena(String nuevaContrasena) {
        return new Usuario(this.id, this.nombre, this.apellido, this.correo, this.telefono, nuevaContrasena, this.fotourl, this.verificado, this.estado, this.roles, this.creadoEn, OffsetDateTime.now());
    }

    public Usuario withEstado(EstadoUsuarios nuevoEstado) {
        return new Usuario(this.id, this.nombre, this.apellido, this.correo, this.telefono, this.contrasena, this.fotourl, this.verificado, nuevoEstado, this.roles, this.creadoEn, OffsetDateTime.now());
    }

    public Usuario withRoles(Set<RolUsuario> nuevosRoles) {
        return new Usuario(this.id, this.nombre, this.apellido, this.correo, this.telefono, this.contrasena, this.fotourl, this.verificado, this.estado, nuevosRoles, this.creadoEn, OffsetDateTime.now());
    }

    public boolean hasRole(RolUsuario rol) {
        return this.roles != null && this.roles.contains(rol);
    }

    public Usuario withUpdatedData(String nuevoNombre, String nuevoApellido, String nuevoTelefono, String nuevaFotoUrl, EstadoUsuarios nuevoEstado, Set<RolUsuario> nuevosRoles) {
        return new Usuario(
            this.id,
            nuevoNombre != null && !nuevoNombre.isBlank() ? nuevoNombre : this.nombre,
            nuevoApellido != null ? nuevoApellido : this.apellido,
            this.correo,
            nuevoTelefono != null ? nuevoTelefono : this.telefono,
            this.contrasena,
            nuevaFotoUrl != null ? nuevaFotoUrl : this.fotourl,
            this.verificado,
            nuevoEstado != null ? nuevoEstado : this.estado,
            nuevosRoles != null && !nuevosRoles.isEmpty() ? nuevosRoles : this.roles,
            this.creadoEn,
            OffsetDateTime.now()
        );
    }
}
