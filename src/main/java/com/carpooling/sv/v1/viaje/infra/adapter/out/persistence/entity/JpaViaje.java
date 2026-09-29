package com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnTransformer;

import com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity.value.EstadoViajeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "viajes")
public class JpaViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "conductor_id", nullable = false)
    private UUID conductorId;

    @Column(name = "vehiculo_id", nullable = false)
    private UUID vehiculoId;

    @Column(name = "ruta_id")
    private UUID rutaId;

    @Column(name = "origen_nombre", nullable = false, length = 150)
    private String origenNombre;

    @Column(name = "origen_geom", nullable = false, columnDefinition = "geography(Point,4326)")
    @ColumnTransformer(read = "ST_AsText(origen_geom)", write = "ST_GeogFromText(?)")
    private String origenGeom;

    @Column(name = "destino_nombre", nullable = false, length = 150)
    private String destinoNombre;

    @Column(name = "destino_geom", nullable = false, columnDefinition = "geography(Point,4326)")
    @ColumnTransformer(read = "ST_AsText(destino_geom)", write = "ST_GeogFromText(?)")
    private String destinoGeom;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "cupos_totales", nullable = false)
    private short cuposTotales;

    @Column(name = "cupos_disponibles", nullable = false)
    private short cuposDisponibles;

    @Column(name = "tarifa_aproximada", precision = 8, scale = 2)
    private BigDecimal tarifaAproximada;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::estado_viaje")
    private EstadoViajeEnum estado;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    public JpaViaje(UUID id, UUID conductorId, UUID vehiculoId, UUID rutaId,
                    String origenNombre, String origenGeom, String destinoNombre, String destinoGeom,
                    LocalDate fechaSalida, LocalTime horaSalida, short cuposTotales, short cuposDisponibles,
                    BigDecimal tarifaAproximada, String descripcion, EstadoViajeEnum estado,
                    OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
        this.id = id;
        this.conductorId = conductorId;
        this.vehiculoId = vehiculoId;
        this.rutaId = rutaId;
        this.origenNombre = origenNombre;
        this.origenGeom = origenGeom;
        this.destinoNombre = destinoNombre;
        this.destinoGeom = destinoGeom;
        this.fechaSalida = fechaSalida;
        this.horaSalida = horaSalida;
        this.cuposTotales = cuposTotales;
        this.cuposDisponibles = cuposDisponibles;
        this.tarifaAproximada = tarifaAproximada;
        this.descripcion = descripcion;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
