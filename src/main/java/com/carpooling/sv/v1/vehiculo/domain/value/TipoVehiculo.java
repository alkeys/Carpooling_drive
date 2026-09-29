package com.carpooling.sv.v1.vehiculo.domain.value;

public enum TipoVehiculo {
    SEDAN,
    SUV,
    HATCHBACK,
    PICKUP,
    CAMIONETA,
    VAN,
    MOTO,
    OTRO;

    public static TipoVehiculo fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return SEDAN;
        }
        try {
            return TipoVehiculo.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTRO;
        }
    }
}
