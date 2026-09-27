package com.carpooling.sv.v1.usuario.domain.value;

import java.util.Objects;

import lombok.Getter;

@Getter
public class EstadoUsuarios {
    private static final String[] ESTADOS = {"ACTIVO", "SUSPENDIDO", "INACTIVO"};

    public static final EstadoUsuarios ACTIVO = new EstadoUsuarios("ACTIVO");
    public static final EstadoUsuarios SUSPENDIDO = new EstadoUsuarios("SUSPENDIDO");
    public static final EstadoUsuarios INACTIVO = new EstadoUsuarios("INACTIVO");

    private final String estadoUsuario;

    public EstadoUsuarios(String estadoUsuario) {
        if (estadoUsuario == null || !isValidEstado(estadoUsuario)) {
            throw new IllegalArgumentException("Estado de usuario inválido: " + estadoUsuario);
        }
        this.estadoUsuario = estadoUsuario.toUpperCase();
    }

    private static boolean isValidEstado(String estado) {
        for (String validEstado : ESTADOS) {
            if (validEstado.equalsIgnoreCase(estado)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstadoUsuarios that = (EstadoUsuarios) o;
        return Objects.equals(estadoUsuario, that.estadoUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estadoUsuario);
    }

    @Override
    public String toString() {
        return estadoUsuario;
    }
}
