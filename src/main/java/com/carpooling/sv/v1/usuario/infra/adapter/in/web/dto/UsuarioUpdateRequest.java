package com.carpooling.sv.v1.usuario.infra.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Solicitud para actualizar los datos de un usuario existente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateRequest {

    @Schema(description = "Nombre(s) del usuario", example = "Juan Carlos")
    private String nombre;

    @Schema(description = "Apellido(s) del usuario", example = "Pérez Gómez")
    private String apellido;

    @Schema(description = "Número telefónico del usuario", example = "+50371234567")
    private String telefono;

    @Schema(description = "URL de la foto de perfil", example = "https://example.com/nueva_foto.jpg")
    private String fotourl;

    @Schema(description = "Nueva contraseña (opcional, mínimo 6 caracteres si se envía)", example = "nuevaClave456")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    @JsonAlias({"contrasena", "contraseña", "password"})
    private String contrasena;

    @Schema(description = "Estado de la cuenta: ACTIVO, SUSPENDIDO o INACTIVO", example = "ACTIVO")
    private String estado;

    @Schema(description = "Roles actualizados del usuario (opcional)", example = "[\"CONDUCTOR\", \"CLIENTE\"]")
    private java.util.Set<String> roles;
}
