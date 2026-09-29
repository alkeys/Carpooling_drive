package com.carpooling.sv.v1.viaje.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.shared.exception.ForbiddenException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.viaje.application.port.in.ActualizarViajeCase;
import com.carpooling.sv.v1.viaje.application.port.in.AgregarPuntoEncuentroCase;
import com.carpooling.sv.v1.viaje.application.port.in.BuscarViajesCase;
import com.carpooling.sv.v1.viaje.application.port.in.CambiarEstadoViajeCase;
import com.carpooling.sv.v1.viaje.application.port.in.GetViajeByIdCase;
import com.carpooling.sv.v1.viaje.application.port.in.GetViajesPorConductorCase;
import com.carpooling.sv.v1.viaje.application.port.in.PublicarViajeCase;
import com.carpooling.sv.v1.viaje.application.port.out.VehiculoVerificationPort;
import com.carpooling.sv.v1.viaje.application.port.out.ViajeRepositoryPort;
import com.carpooling.sv.v1.viaje.domain.PuntoEncuentro;
import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.Coordenada;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;

@Service
@Transactional
public class ViajeService implements
        PublicarViajeCase,
        GetViajeByIdCase,
        BuscarViajesCase,
        GetViajesPorConductorCase,
        ActualizarViajeCase,
        CambiarEstadoViajeCase,
        AgregarPuntoEncuentroCase {

    private final ViajeRepositoryPort viajeRepositoryPort;
    private final VehiculoVerificationPort vehiculoVerificationPort;
    private final SecurityContextHelper securityHelper;

    public ViajeService(
            ViajeRepositoryPort viajeRepositoryPort,
            VehiculoVerificationPort vehiculoVerificationPort,
            SecurityContextHelper securityHelper) {
        this.viajeRepositoryPort = viajeRepositoryPort;
        this.vehiculoVerificationPort = vehiculoVerificationPort;
        this.securityHelper = securityHelper;
    }

    @Override
    public Viaje publicarViaje(Viaje viaje, List<PuntoEncuentro> puntosEncuentro) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        if (!securityHelper.hasRole("CONDUCTOR") && !securityHelper.isAdmin()) {
            throw new ForbiddenException("Debes tener el rol de CONDUCTOR para publicar viajes");
        }

        // Anti-IDOR: Validar que el vehículo exista, esté activo y pertenezca al conductor
        if (!vehiculoVerificationPort.isVehiculoValidoParaConductor(viaje.vehiculoId(), currentUserId, viaje.cuposTotales())) {
            throw new ForbiddenException("El vehículo especificado no te pertenece o no tiene la capacidad suficiente");
        }

        if (viaje.fechaSalida().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de salida no puede ser anterior a hoy");
        }

        Viaje viajeAguardar = new Viaje(
            null,
            currentUserId,
            viaje.vehiculoId(),
            viaje.rutaId(),
            viaje.origenNombre(),
            viaje.origenCoordenada(),
            viaje.destinoNombre(),
            viaje.destinoCoordenada(),
            viaje.fechaSalida(),
            viaje.horaSalida(),
            viaje.cuposTotales(),
            viaje.cuposTotales(), // Inicialmente disponibles = totales
            viaje.tarifaAproximada(),
            viaje.descripcion(),
            EstadoViaje.PUBLICADO,
            List.of(),
            null,
            null
        );

        Viaje guardado = viajeRepositoryPort.save(viajeAguardar);

        List<PuntoEncuentro> puntosGuardados = new ArrayList<>();
        if (puntosEncuentro != null && !puntosEncuentro.isEmpty()) {
            for (PuntoEncuentro p : puntosEncuentro) {
                PuntoEncuentro conViaje = p.withViajeId(guardado.id());
                puntosGuardados.add(viajeRepositoryPort.savePuntoEncuentro(conViaje));
            }
        }

        return guardado.withPuntosEncuentro(puntosGuardados);
    }

    @Override
    @Transactional(readOnly = true)
    public Viaje getViajeById(UUID id) {
        Viaje viaje = viajeRepositoryPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + id));

        List<PuntoEncuentro> puntos = viajeRepositoryPort.findPuntosPorViajeId(id);
        return viaje.withPuntosEncuentro(puntos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viaje> buscarViajes(
            Double origenLat,
            Double origenLon,
            Double radioOrigenKm,
            Double destinoLat,
            Double destinoLon,
            Double radioDestinoKm,
            String origenTexto,
            String destinoTexto,
            LocalDate fecha,
            Integer cuposMinimos) {

        int cupos = (cuposMinimos != null && cuposMinimos > 0) ? cuposMinimos : 1;
        List<Viaje> viajes;

        if (origenLat != null && origenLon != null && destinoLat != null && destinoLon != null) {
            double rOrigenMetros = (radioOrigenKm != null && radioOrigenKm > 0 ? radioOrigenKm : 10.0) * 1000.0;
            double rDestinoMetros = (radioDestinoKm != null && radioDestinoKm > 0 ? radioDestinoKm : 10.0) * 1000.0;
            viajes = viajeRepositoryPort.buscarViajesGeo(
                origenLon, origenLat, rOrigenMetros,
                destinoLon, destinoLat, rDestinoMetros,
                fecha, cupos
            );
        } else {
            viajes = viajeRepositoryPort.buscarViajesTexto(origenTexto, destinoTexto, fecha, cupos);
        }

        // Cargar puntos de encuentro para cada viaje
        return viajes.stream()
            .map(v -> v.withPuntosEncuentro(viajeRepositoryPort.findPuntosPorViajeId(v.id())))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viaje> getViajesPorConductor(UUID conductorId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el conductor o un ADMIN pueden listar los viajes de su historial
        if (!conductorId.equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para consultar los viajes de otro conductor");
        }

        return viajeRepositoryPort.findByConductorId(conductorId).stream()
            .map(v -> v.withPuntosEncuentro(viajeRepositoryPort.findPuntosPorViajeId(v.id())))
            .toList();
    }

    @Override
    public Viaje actualizarViaje(
            UUID id,
            String origenNombre,
            Coordenada origenCoordenada,
            String destinoNombre,
            Coordenada destinoCoordenada,
            LocalDate fechaSalida,
            LocalTime horaSalida,
            BigDecimal tarifaAproximada,
            String descripcion) {

        Viaje existente = getViajeById(id);
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el conductor creador o ADMIN pueden modificar el viaje
        if (!existente.conductorId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para modificar este viaje");
        }

        Viaje actualizado = existente.withUpdatedData(
            origenNombre,
            origenCoordenada,
            destinoNombre,
            destinoCoordenada,
            fechaSalida,
            horaSalida,
            tarifaAproximada,
            descripcion
        );

        return viajeRepositoryPort.update(actualizado).withPuntosEncuentro(existente.puntosEncuentro());
    }

    @Override
    public Viaje cambiarEstadoViaje(UUID id, EstadoViaje nuevoEstado) {
        Viaje existente = getViajeById(id);
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el conductor creador o ADMIN pueden cambiar de estado el viaje
        if (!existente.conductorId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para alterar el estado de este viaje");
        }

        Viaje modificado = existente.withEstado(nuevoEstado);
        return viajeRepositoryPort.update(modificado).withPuntosEncuentro(existente.puntosEncuentro());
    }

    @Override
    public PuntoEncuentro agregarPuntoEncuentro(UUID viajeId, PuntoEncuentro punto) {
        Viaje existente = getViajeById(viajeId);
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el conductor creador o ADMIN pueden añadir puntos de encuentro
        if (!existente.conductorId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para agregar puntos de encuentro a este viaje");
        }

        if (existente.estado() != EstadoViaje.PUBLICADO) {
            throw new IllegalStateException("Solo se pueden agregar paradas intermedias a viajes en estado PUBLICADO");
        }

        PuntoEncuentro aGuardar = punto.withViajeId(viajeId);
        return viajeRepositoryPort.savePuntoEncuentro(aGuardar);
    }
}
