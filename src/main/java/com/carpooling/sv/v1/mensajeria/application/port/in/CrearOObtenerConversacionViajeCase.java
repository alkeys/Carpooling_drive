package com.carpooling.sv.v1.mensajeria.application.port.in;

import java.util.UUID;

import com.carpooling.sv.v1.mensajeria.domain.Conversacion;

public interface CrearOObtenerConversacionViajeCase {
    Conversacion obtenerOCrearConversacionPorViaje(UUID viajeId);
}
