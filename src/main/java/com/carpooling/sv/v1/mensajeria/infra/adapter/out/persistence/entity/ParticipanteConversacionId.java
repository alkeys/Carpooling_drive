package com.carpooling.sv.v1.mensajeria.infra.adapter.out.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ParticipanteConversacionId implements Serializable {

    @Column(name = "conversacion_id", nullable = false)
    private UUID conversacionId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    public ParticipanteConversacionId(UUID conversacionId, UUID usuarioId) {
        this.conversacionId = conversacionId;
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipanteConversacionId that = (ParticipanteConversacionId) o;
        return Objects.equals(conversacionId, that.conversacionId) && Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversacionId, usuarioId);
    }
}
