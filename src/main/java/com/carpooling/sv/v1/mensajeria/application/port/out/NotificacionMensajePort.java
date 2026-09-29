package com.carpooling.sv.v1.mensajeria.application.port.out;

import com.carpooling.sv.v1.mensajeria.domain.Mensaje;

public interface NotificacionMensajePort {
    void notificarNuevoMensaje(Mensaje mensaje);
}
