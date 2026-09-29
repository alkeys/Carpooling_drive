package com.carpooling.sv.v1.viaje.infra.adapter.out.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnTransformer;

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
@Table(name = "puntos_encuentro")
public class JpaPuntoEncuentro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "viaje_id", nullable = false)
    private UUID viajeId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ubicacion", nullable = false, columnDefinition = "geography(Point,4326)")
    @ColumnTransformer(read = "ST_AsText(ubicacion)", write = "ST_GeogFromText(?)")
    private String ubicacion;

    @Column(name = "orden", nullable = false)
    private short orden;

    @Column(name = "permite_subida", nullable = false)
    private boolean permiteSubida;

    @Column(name = "permite_bajada", nullable = false)
    private boolean permiteBajada;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    public JpaPuntoEncuentro(UUID id, UUID viajeId, String nombre, String descripcion,
                            String ubicacion, short orden, boolean permiteSubida,
                            boolean permiteBajada, OffsetDateTime creadoEn) {
        this.id = id;
        this.viajeId = viajeId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.orden = orden;
        this.permiteSubida = permiteSubida;
        this.permiteBajada = permiteBajada;
        this.creadoEn = creadoEn;
    }
}
