package com.carpooling.sv.v1.viaje.domain.value;

public enum EstadoViaje {
    PUBLICADO,
    COMPLETO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO;

    public static EstadoViaje fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return PUBLICADO;
        }
        try {
            return EstadoViaje.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de viaje inválido: " + valor);
        }
    }
}
