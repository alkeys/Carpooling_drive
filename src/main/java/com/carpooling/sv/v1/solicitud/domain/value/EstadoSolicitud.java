package com.carpooling.sv.v1.solicitud.domain.value;

public enum EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    CANCELADA;

    public static EstadoSolicitud fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return PENDIENTE;
        }
        try {
            return EstadoSolicitud.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de solicitud inválido: " + valor);
        }
    }
}
