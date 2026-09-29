package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "participantes_conversacion")
public class JpaParticipanteConversacion {

    @EmbeddedId
    private ParticipanteConversacionId id;

    @Column(name = "unido_en", nullable = false)
    private OffsetDateTime unidoEn;

    public JpaParticipanteConversacion(ParticipanteConversacionId id, OffsetDateTime unidoEn) {
        this.id = id;
        this.unidoEn = unidoEn;
    }
}
