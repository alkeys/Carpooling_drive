package com.carpooling.sv.v1.vehiculo.infra.adapter.out.persistence.entity;

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
@Table(name = "vehiculos")
public class JpaVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "propietario_id", nullable = false)
    private UUID propietarioId;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "placa", length = 20, unique = true)
    private String placa;

    @Column(name = "capacidad_pasajeros", nullable = false)
    private short capacidadPasajeros;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    public JpaVehiculo(UUID id, UUID propietarioId, String marca, String modelo, Integer anio, String color,
                       String tipo, String placa, short capacidadPasajeros, String fotoUrl, boolean activo,
                       OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
        this.id = id;
        this.propietarioId = propietarioId;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.tipo = tipo;
        this.placa = placa;
        this.capacidadPasajeros = capacidadPasajeros;
        this.fotoUrl = fotoUrl;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
