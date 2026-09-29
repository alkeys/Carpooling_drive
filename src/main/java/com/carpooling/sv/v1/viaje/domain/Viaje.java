package com.carpooling.sv.v1.viaje.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.carpooling.sv.v1.viaje.domain.value.Coordenada;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;

public record Viaje(
    UUID id,
    UUID conductorId,
    UUID vehiculoId,
    UUID rutaId,
    String origenNombre,
    Coordenada origenCoordenada,
    String destinoNombre,
    Coordenada destinoCoordenada,
    LocalDate fechaSalida,
    LocalTime horaSalida,
    int cuposTotales,
    int cuposDisponibles,
    BigDecimal tarifaAproximada,
    String descripcion,
    EstadoViaje estado,
    List<PuntoEncuentro> puntosEncuentro,
    OffsetDateTime creadoEn,
    OffsetDateTime actualizadoEn
) {
    public Viaje {
        if (conductorId == null) {
            throw new IllegalArgumentException("El viaje debe tener un conductor asignado");
        }
        if (vehiculoId == null) {
            throw new IllegalArgumentException("El viaje debe tener un vehículo asociado");
        }
        if (origenNombre == null || origenNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del origen no puede ser vacío");
        }
        if (origenCoordenada == null) {
            throw new IllegalArgumentException("Las coordenadas de origen son obligatorias");
        }
        if (destinoNombre == null || destinoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del destino no puede ser vacío");
        }
        if (destinoCoordenada == null) {
            throw new IllegalArgumentException("Las coordenadas de destino son obligatorias");
        }
        if (fechaSalida == null) {
            throw new IllegalArgumentException("La fecha de salida es obligatoria");
        }
        if (horaSalida == null) {
            throw new IllegalArgumentException("La hora de salida es obligatoria");
        }
        if (cuposTotales <= 0) {
            throw new IllegalArgumentException("Los cupos totales deben ser mayores a 0");
        }
        if (cuposDisponibles < 0 || cuposDisponibles > cuposTotales) {
            throw new IllegalArgumentException("Los cupos disponibles deben estar entre 0 y los cupos totales");
        }
        if (tarifaAproximada != null && tarifaAproximada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tarifa aproximada no puede ser negativa");
        }
        if (estado == null) {
            estado = EstadoViaje.PUBLICADO;
        }
        if (puntosEncuentro == null) {
            puntosEncuentro = List.of();
        } else {
            puntosEncuentro = Collections.unmodifiableList(new ArrayList<>(puntosEncuentro));
        }
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (actualizadoEn == null) {
            actualizadoEn = OffsetDateTime.now();
        }
    }

    public Viaje withId(UUID nuevoId) {
        return new Viaje(
            nuevoId, this.conductorId, this.vehiculoId, this.rutaId,
            this.origenNombre, this.origenCoordenada, this.destinoNombre, this.destinoCoordenada,
            this.fechaSalida, this.horaSalida, this.cuposTotales, this.cuposDisponibles,
            this.tarifaAproximada, this.descripcion, this.estado, this.puntosEncuentro,
            this.creadoEn, this.actualizadoEn
        );
    }

    public Viaje withPuntosEncuentro(List<PuntoEncuentro> nuevosPuntos) {
        return new Viaje(
            this.id, this.conductorId, this.vehiculoId, this.rutaId,
            this.origenNombre, this.origenCoordenada, this.destinoNombre, this.destinoCoordenada,
            this.fechaSalida, this.horaSalida, this.cuposTotales, this.cuposDisponibles,
            this.tarifaAproximada, this.descripcion, this.estado, nuevosPuntos,
            this.creadoEn, OffsetDateTime.now()
        );
    }

    public Viaje withEstado(EstadoViaje nuevoEstado) {
        if (this.estado == EstadoViaje.FINALIZADO || this.estado == EstadoViaje.CANCELADO) {
            throw new IllegalStateException("No se puede cambiar el estado de un viaje " + this.estado);
        }
        return new Viaje(
            this.id, this.conductorId, this.vehiculoId, this.rutaId,
            this.origenNombre, this.origenCoordenada, this.destinoNombre, this.destinoCoordenada,
            this.fechaSalida, this.horaSalida, this.cuposTotales, this.cuposDisponibles,
            this.tarifaAproximada, this.descripcion, nuevoEstado, this.puntosEncuentro,
            this.creadoEn, OffsetDateTime.now()
        );
    }

    public Viaje withDescontarCupos(int cantidadADescontar) {
        if (cantidadADescontar <= 0) {
            throw new IllegalArgumentException("La cantidad de cupos a descontar debe ser mayor a 0");
        }
        if (this.cuposDisponibles < cantidadADescontar) {
            throw new IllegalStateException("No hay suficientes cupos disponibles para esta solicitud");
        }
        int restantes = this.cuposDisponibles - cantidadADescontar;
        EstadoViaje nuevoEstado = restantes == 0 ? EstadoViaje.COMPLETO : this.estado;

        return new Viaje(
            this.id, this.conductorId, this.vehiculoId, this.rutaId,
            this.origenNombre, this.origenCoordenada, this.destinoNombre, this.destinoCoordenada,
            this.fechaSalida, this.horaSalida, this.cuposTotales, restantes,
            this.tarifaAproximada, this.descripcion, nuevoEstado, this.puntosEncuentro,
            this.creadoEn, OffsetDateTime.now()
        );
    }

    public Viaje withRestaurarCupos(int cantidadARestaurar) {
        if (cantidadARestaurar <= 0) {
            throw new IllegalArgumentException("La cantidad de cupos a restaurar debe ser mayor a 0");
        }
        int nuevosCupos = Math.min(this.cuposTotales, this.cuposDisponibles + cantidadARestaurar);
        EstadoViaje nuevoEstado = this.estado;
        if (this.estado == EstadoViaje.COMPLETO && nuevosCupos > 0) {
            nuevoEstado = EstadoViaje.PUBLICADO;
        }

        return new Viaje(
            this.id, this.conductorId, this.vehiculoId, this.rutaId,
            this.origenNombre, this.origenCoordenada, this.destinoNombre, this.destinoCoordenada,
            this.fechaSalida, this.horaSalida, this.cuposTotales, nuevosCupos,
            this.tarifaAproximada, this.descripcion, nuevoEstado, this.puntosEncuentro,
            this.creadoEn, OffsetDateTime.now()
        );
    }

    public Viaje withUpdatedData(
            String nuevoOrigenNombre,
            Coordenada nuevoOrigenCoordenada,
            String nuevoDestinoNombre,
            Coordenada nuevoDestinoCoordenada,
            LocalDate nuevaFechaSalida,
            LocalTime nuevaHoraSalida,
            BigDecimal nuevaTarifaAproximada,
            String nuevaDescripcion) {
        if (this.estado != EstadoViaje.PUBLICADO) {
            throw new IllegalStateException("Solo se pueden modificar viajes en estado PUBLICADO");
        }
        return new Viaje(
            this.id,
            this.conductorId,
            this.vehiculoId,
            this.rutaId,
            nuevoOrigenNombre != null && !nuevoOrigenNombre.isBlank() ? nuevoOrigenNombre : this.origenNombre,
            nuevoOrigenCoordenada != null ? nuevoOrigenCoordenada : this.origenCoordenada,
            nuevoDestinoNombre != null && !nuevoDestinoNombre.isBlank() ? nuevoDestinoNombre : this.destinoNombre,
            nuevoDestinoCoordenada != null ? nuevoDestinoCoordenada : this.destinoCoordenada,
            nuevaFechaSalida != null ? nuevaFechaSalida : this.fechaSalida,
            nuevaHoraSalida != null ? nuevaHoraSalida : this.horaSalida,
            this.cuposTotales,
            this.cuposDisponibles,
            nuevaTarifaAproximada != null ? nuevaTarifaAproximada : this.tarifaAproximada,
            nuevaDescripcion != null ? nuevaDescripcion : this.descripcion,
            this.estado,
            this.puntosEncuentro,
            this.creadoEn,
            OffsetDateTime.now()
        );
    }
}
