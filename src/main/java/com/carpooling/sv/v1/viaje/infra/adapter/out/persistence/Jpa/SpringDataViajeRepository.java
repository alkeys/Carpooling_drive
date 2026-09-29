package com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.Jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.JpaViaje;

@Repository
public interface SpringDataViajeRepository extends JpaRepository<JpaViaje, UUID> {

    List<JpaViaje> findByConductorIdOrderByFechaSalidaDescHoraSalidaDesc(UUID conductorId);

    @Query(value = """
        SELECT v.* FROM viajes v
        WHERE v.estado = 'PUBLICADO'
          AND v.cupos_disponibles >= :cuposMinimos
          AND v.fecha_salida = :fecha
          AND ST_DWithin(v.origen_geom, ST_SetSRID(ST_MakePoint(:origenLon, :origenLat), 4326), :radioOrigenMetros)
          AND ST_DWithin(v.destino_geom, ST_SetSRID(ST_MakePoint(:destinoLon, :destinoLat), 4326), :radioDestinoMetros)
        ORDER BY v.fecha_salida ASC, v.hora_salida ASC
        """, nativeQuery = true)
    List<JpaViaje> buscarViajesGeoConFecha(
        @Param("origenLon") Double origenLon,
        @Param("origenLat") Double origenLat,
        @Param("radioOrigenMetros") Double radioOrigenMetros,
        @Param("destinoLon") Double destinoLon,
        @Param("destinoLat") Double destinoLat,
        @Param("radioDestinoMetros") Double radioDestinoMetros,
        @Param("fecha") LocalDate fecha,
        @Param("cuposMinimos") int cuposMinimos
    );

    @Query(value = """
        SELECT v.* FROM viajes v
        WHERE v.estado = 'PUBLICADO'
          AND v.cupos_disponibles >= :cuposMinimos
          AND ST_DWithin(v.origen_geom, ST_SetSRID(ST_MakePoint(:origenLon, :origenLat), 4326), :radioOrigenMetros)
          AND ST_DWithin(v.destino_geom, ST_SetSRID(ST_MakePoint(:destinoLon, :destinoLat), 4326), :radioDestinoMetros)
        ORDER BY v.fecha_salida ASC, v.hora_salida ASC
        """, nativeQuery = true)
    List<JpaViaje> buscarViajesGeoSinFecha(
        @Param("origenLon") Double origenLon,
        @Param("origenLat") Double origenLat,
        @Param("radioOrigenMetros") Double radioOrigenMetros,
        @Param("destinoLon") Double destinoLon,
        @Param("destinoLat") Double destinoLat,
        @Param("radioDestinoMetros") Double radioDestinoMetros,
        @Param("cuposMinimos") int cuposMinimos
    );

    @Query("""
        SELECT v FROM JpaViaje v
        WHERE v.estado = com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.value.EstadoViajeEnum.PUBLICADO
          AND v.cuposDisponibles >= :cuposMinimos
        ORDER BY v.fechaSalida ASC, v.horaSalida ASC
        """)
    List<JpaViaje> buscarViajesDisponibles(
        @Param("cuposMinimos") int cuposMinimos
    );
}
