package com.carpooling.sv.v1.solicitud.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.carpooling.sv.v1.solicitud.domain.value.EstadoSolicitud;

public record SolicitudViaje(
    UUID id,
    UUID viajeId,
    UUID pasajeroId,
    UUID puntoSubidaId,
    UUID puntoBajadaId,
    int cantidadCupos,
    EstadoSolicitud estado,
    String mensaje,
    OffsetDateTime creadoEn,
    OffsetDateTime respondidoEn,
    OffsetDateTime actualizadoEn
) {
    public SolicitudViaje {
        if (viajeId == null) {
            throw new IllegalArgumentException("El ID del viaje es obligatorio");
        }
        if (pasajeroId == null) {
            throw new IllegalArgumentException("El ID del pasajero es obligatorio");
        }
        if (cantidadCupos <= 0) {
            throw new IllegalArgumentException("La cantidad de cupos debe ser mayor a 0");
        }
        if (estado == null) {
            estado = EstadoSolicitud.PENDIENTE;
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (actualizadoEn == null) {
            actualizadoEn = OffsetDateTime.now();
        }
    }

    public SolicitudViaje withId(UUID nuevoId) {
        return new SolicitudViaje(
            nuevoId, this.viajeId, this.pasajeroId, this.puntoSubidaId, this.puntoBajadaId,
            this.cantidadCupos, this.estado, this.mensaje, this.creadoEn, this.respondidoEn, this.actualizadoEn
        );
    }

    public SolicitudViaje withAceptada() {
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aceptar solicitudes en estado PENDIENTE");
        }
        return new SolicitudViaje(
            this.id, this.viajeId, this.pasajeroId, this.puntoSubidaId, this.puntoBajadaId,
            this.cantidadCupos, EstadoSolicitud.ACEPTADA, this.mensaje, this.creadoEn, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    public SolicitudViaje withRechazada() {
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes en estado PENDIENTE");
        }
        return new SolicitudViaje(
            this.id, this.viajeId, this.pasajeroId, this.puntoSubidaId, this.puntoBajadaId,
            this.cantidadCupos, EstadoSolicitud.RECHAZADA, this.mensaje, this.creadoEn, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    public SolicitudViaje withCancelada() {
        if (this.estado != EstadoSolicitud.PENDIENTE && this.estado != EstadoSolicitud.ACEPTADA) {
            throw new IllegalStateException("Solo se pueden cancelar solicitudes PENDIENTES o ACEPTADAS");
        }
        return new SolicitudViaje(
            this.id, this.viajeId, this.pasajeroId, this.puntoSubidaId, this.puntoBajadaId,
            this.cantidadCupos, EstadoSolicitud.CANCELADA, this.mensaje, this.creadoEn, this.respondidoEn, OffsetDateTime.now()
        );
    }

    public SolicitudViaje withReactivada(int nuevaCantidadCupos, UUID nuevoPuntoSubida, UUID nuevoPuntoBajada, String nuevoMensaje) {
        return new SolicitudViaje(
            this.id, this.viajeId, this.pasajeroId, nuevoPuntoSubida, nuevoPuntoBajada,
            nuevaCantidadCupos, EstadoSolicitud.PENDIENTE, nuevoMensaje, this.creadoEn, null, OffsetDateTime.now()
        );
    }
}
