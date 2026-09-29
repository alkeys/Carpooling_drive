package com.carpooling.sv.v1.viaje.application.port.in;

import java.util.List;

import com.carpooling.sv.v1.viaje.domain.PuntoEncuentro;
import com.carpooling.sv.v1.viaje.domain.Viaje;

public interface PublicarViajeCase {
    Viaje publicarViaje(Viaje viaje, List<PuntoEncuentro> puntosEncuentro);
}
