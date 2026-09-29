package com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.Adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.viaje.application.port.out.ViajeRepositoryPort;
import com.carpooling.sv.v1.viaje.domain.PuntoEncuentro;
import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.Coordenada;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;
import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.Jpa.SpringDataPuntoEncuentroRepository;
import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.Jpa.SpringDataViajeRepository;
import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.JpaPuntoEncuentro;
import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.JpaViaje;
import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.value.EstadoViajeEnum;

@Component
public class JpaViajeAdapter implements ViajeRepositoryPort {

    private final SpringDataViajeRepository viajeRepository;
    private final SpringDataPuntoEncuentroRepository puntoEncuentroRepository;

    public JpaViajeAdapter(
            SpringDataViajeRepository viajeRepository,
            SpringDataPuntoEncuentroRepository puntoEncuentroRepository) {
        this.viajeRepository = viajeRepository;
        this.puntoEncuentroRepository = puntoEncuentroRepository;
    }

    @Override
    public Viaje save(Viaje viaje) {
        JpaViaje jpa = toJpaViaje(viaje);
        JpaViaje saved = viajeRepository.save(jpa);
        return toDomainViaje(saved);
    }

    @Override
    public Optional<Viaje> findById(UUID id) {
        return viajeRepository.findById(id).map(this::toDomainViaje);
    }

    @Override
    public List<Viaje> findByConductorId(UUID conductorId) {
        return viajeRepository.findByConductorIdOrderByFechaSalidaDescHoraSalidaDesc(conductorId).stream()
            .map(this::toDomainViaje)
            .toList();
    }

    @Override
    public List<Viaje> buscarViajesGeo(
            Double origenLon,
            Double origenLat,
            Double radioOrigenMetros,
            Double destinoLon,
            Double destinoLat,
            Double radioDestinoMetros,
            LocalDate fecha,
            int cuposMinimos) {
        List<JpaViaje> resultados = (fecha != null)
            ? viajeRepository.buscarViajesGeoConFecha(origenLon, origenLat, radioOrigenMetros, destinoLon, destinoLat, radioDestinoMetros, fecha, cuposMinimos)
            : viajeRepository.buscarViajesGeoSinFecha(origenLon, origenLat, radioOrigenMetros, destinoLon, destinoLat, radioDestinoMetros, cuposMinimos);

        return resultados.stream()
            .map(this::toDomainViaje)
            .toList();
    }

    @Override
    public List<Viaje> buscarViajesTexto(String origenTexto, String destinoTexto, LocalDate fecha, int cuposMinimos) {
        String oTexto = (origenTexto != null && !origenTexto.isBlank()) ? origenTexto.trim().toLowerCase() : null;
        String dTexto = (destinoTexto != null && !destinoTexto.isBlank()) ? destinoTexto.trim().toLowerCase() : null;

        return viajeRepository.buscarViajesDisponibles(cuposMinimos).stream()
            .filter(v -> fecha == null || v.getFechaSalida().equals(fecha))
            .filter(v -> oTexto == null || (v.getOrigenNombre() != null && v.getOrigenNombre().toLowerCase().contains(oTexto)))
            .filter(v -> dTexto == null || (v.getDestinoNombre() != null && v.getDestinoNombre().toLowerCase().contains(dTexto)))
            .map(this::toDomainViaje)
            .toList();
    }

    @Override
    public Viaje update(Viaje viaje) {
        JpaViaje jpa = toJpaViaje(viaje);
        JpaViaje updated = viajeRepository.save(jpa);
        return toDomainViaje(updated);
    }

    @Override
    public PuntoEncuentro savePuntoEncuentro(PuntoEncuentro punto) {
        JpaPuntoEncuentro jpa = toJpaPuntoEncuentro(punto);
        JpaPuntoEncuentro saved = puntoEncuentroRepository.save(jpa);
        return toDomainPuntoEncuentro(saved);
    }

    @Override
    public List<PuntoEncuentro> findPuntosPorViajeId(UUID viajeId) {
        return puntoEncuentroRepository.findByViajeIdOrderByOrdenAsc(viajeId).stream()
            .map(this::toDomainPuntoEncuentro)
            .toList();
    }

    @Override
    public void deletePuntosPorViajeId(UUID viajeId) {
        puntoEncuentroRepository.deleteByViajeId(viajeId);
    }

    private JpaViaje toJpaViaje(Viaje v) {
        return new JpaViaje(
            v.id(),
            v.conductorId(),
            v.vehiculoId(),
            v.rutaId(),
            v.origenNombre(),
            v.origenCoordenada().toWkt(),
            v.destinoNombre(),
            v.destinoCoordenada().toWkt(),
            v.fechaSalida(),
            v.horaSalida(),
            (short) v.cuposTotales(),
            (short) v.cuposDisponibles(),
            v.tarifaAproximada(),
            v.descripcion(),
            EstadoViajeEnum.valueOf(v.estado().name()),
            v.creadoEn(),
            v.actualizadoEn()
        );
    }

    private Viaje toDomainViaje(JpaViaje j) {
        return new Viaje(
            j.getId(),
            j.getConductorId(),
            j.getVehiculoId(),
            j.getRutaId(),
            j.getOrigenNombre(),
            Coordenada.fromWkt(j.getOrigenGeom()),
            j.getDestinoNombre(),
            Coordenada.fromWkt(j.getDestinoGeom()),
            j.getFechaSalida(),
            j.getHoraSalida(),
            j.getCuposTotales(),
            j.getCuposDisponibles(),
            j.getTarifaAproximada(),
            j.getDescripcion(),
            EstadoViaje.valueOf(j.getEstado().name()),
            List.of(),
            j.getCreadoEn(),
            j.getActualizadoEn()
        );
    }

    private JpaPuntoEncuentro toJpaPuntoEncuentro(PuntoEncuentro p) {
        return new JpaPuntoEncuentro(
            p.id(),
            p.viajeId(),
            p.nombre(),
            p.descripcion(),
            p.ubicacion().toWkt(),
            (short) p.orden(),
            p.permiteSubida(),
            p.permiteBajada(),
            p.creadoEn()
        );
    }

    private PuntoEncuentro toDomainPuntoEncuentro(JpaPuntoEncuentro j) {
        return new PuntoEncuentro(
            j.getId(),
            j.getViajeId(),
            j.getNombre(),
            j.getDescripcion(),
            Coordenada.fromWkt(j.getUbicacion()),
            j.getOrden(),
            j.isPermiteSubida(),
            j.isPermiteBajada(),
            j.getCreadoEn()
        );
    }
}
