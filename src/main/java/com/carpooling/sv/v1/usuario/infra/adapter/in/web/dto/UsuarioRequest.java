package com.carpooling.sv.v1.usuario.infra.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Solicitud para el registro y creación de un nuevo usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @Schema(description = "Nombre(s) del usuario", example = "Juan")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(description = "Apellido(s) del usuario", example = "Pérez")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Schema(description = "Correo electrónico único del usuario", example = "juan.perez@example.com")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String correo;

    @Schema(description = "Número telefónico único del usuario", example = "+50371234567")
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Schema(description = "URL de la foto de perfil (opcional)", example = "https://example.com/foto.jpg")
    private String fotourl;

    @Schema(description = "Contraseña de la cuenta (mínimo 6 caracteres)", example = "secreto123")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @JsonAlias({"contrasena", "contraseña", "password"})
    private String contrasena;

    @Schema(description = "Roles asignados al usuario (opcional, por defecto ['CLIENTE', 'USUARIO'])", example = "[\"CLIENTE\", \"USUARIO\"]")
    private java.util.Set<String> roles;
}