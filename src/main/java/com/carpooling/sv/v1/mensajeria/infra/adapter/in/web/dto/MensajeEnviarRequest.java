package com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Contenido de un mensaje a enviar en la conversación grupal")
public record MensajeEnviarRequest(
    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    @Schema(description = "Texto del mensaje", example = "Hola a todos, ¿en qué punto exacto nos reunimos?")
    String contenido
) {}
