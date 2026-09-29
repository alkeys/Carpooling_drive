package com.carpooling.sv.v1.viaje.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.Coordenada;

public interface ActualizarViajeCase {
    Viaje actualizarViaje(
        UUID id,
        String origenNombre,
        Coordenada origenCoordenada,
        String destinoNombre,
        Coordenada destinoCoordenada,
        LocalDate fechaSalida,
        LocalTime horaSalida,
        BigDecimal tarifaAproximada,
        String descripcion
    );
}
