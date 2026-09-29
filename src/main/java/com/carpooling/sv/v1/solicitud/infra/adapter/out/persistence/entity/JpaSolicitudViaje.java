package com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnTransformer;

import com.carpooling.sv.v1.solicitud.infra.adapter.out.persistence.entity.value.EstadoSolicitudEnum;

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
@Table(name = "solicitudes_viaje")
public class JpaSolicitudViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "viaje_id", nullable = false)
    private UUID viajeId;

    @Column(name = "pasajero_id", nullable = false)
    private UUID pasajeroId;

    @Column(name = "punto_subida_id")
    private UUID puntoSubidaId;

    @Column(name = "punto_bajada_id")
    private UUID puntoBajadaId;

    @Column(name = "cantidad_cupos", nullable = false)
    private short cantidadCupos;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::estado_solicitud")
    private EstadoSolicitudEnum estado;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "respondido_en")
    private OffsetDateTime respondidoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    public JpaSolicitudViaje(UUID id, UUID viajeId, UUID pasajeroId, UUID puntoSubidaId, UUID puntoBajadaId,
                            short cantidadCupos, EstadoSolicitudEnum estado, String mensaje,
                            OffsetDateTime creadoEn, OffsetDateTime respondidoEn, OffsetDateTime actualizadoEn) {
        this.id = id;
        this.viajeId = viajeId;
        this.pasajeroId = pasajeroId;
        this.puntoSubidaId = puntoSubidaId;
        this.puntoBajadaId = puntoBajadaId;
        this.cantidadCupos = cantidadCupos;
        this.estado = estado;
        this.mensaje = mensaje;
        this.creadoEn = creadoEn;
        this.respondidoEn = respondidoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
