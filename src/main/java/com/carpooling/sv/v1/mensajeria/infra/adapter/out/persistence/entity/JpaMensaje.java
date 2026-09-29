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
@Table(name = "mensajes")
public class JpaMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "conversacion_id", nullable = false)
    private UUID conversacionId;

    @Column(name = "remitente_id", nullable = false)
    private UUID remitenteId;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "enviado_en", nullable = false)
    private OffsetDateTime enviadoEn;

    @Column(name = "leido_en")
    private OffsetDateTime leidoEn;

    public JpaMensaje(UUID id, UUID conversacionId, UUID remitenteId, String contenido,
                      OffsetDateTime enviadoEn, OffsetDateTime leidoEn) {
        this.id = id;
        this.conversacionId = conversacionId;
        this.remitenteId = remitenteId;
        this.contenido = contenido;
        this.enviadoEn = enviadoEn;
        this.leidoEn = leidoEn;
    }
}
