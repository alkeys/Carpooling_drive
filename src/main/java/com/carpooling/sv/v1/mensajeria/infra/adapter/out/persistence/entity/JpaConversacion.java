package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity;

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
@Table(name = "conversaciones")
public class JpaConversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "viaje_id", nullable = false, unique = true)
    private UUID viajeId;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    public JpaConversacion(UUID id, UUID viajeId, OffsetDateTime creadoEn) {
        this.id = id;
        this.viajeId = viajeId;
        this.creadoEn = creadoEn;
    }
}
