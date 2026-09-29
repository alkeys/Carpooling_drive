package com.carpooling.sv.v1.calificacion.infra.adapter.out.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "calificaciones")
public class JpaCalificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "viaje_id", nullable = false)
    private UUID viajeId;

    @Column(name = "evaluador_id", nullable = false)
    private UUID evaluadorId;

    @Column(name = "evaluado_id", nullable = false)
    private UUID evaluadoId;

    @Column(name = "puntuacion", nullable = false)
    private short puntuacion;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    public JpaCalificacion(UUID id, UUID viajeId, UUID evaluadorId, UUID evaluadoId,
                           short puntuacion, String comentario, OffsetDateTime creadoEn) {
        this.id = id;
        this.viajeId = viajeId;
        this.evaluadorId = evaluadorId;
        this.evaluadoId = evaluadoId;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.creadoEn = creadoEn;
    }
}
