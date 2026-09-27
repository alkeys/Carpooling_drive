package com.carpooling.sv.v1.auth.infra.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Credenciales para iniciar sesión en la plataforma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "Correo electrónico del usuario", example = "usuario@ejemplo.com")
    @NotBlank(message = "El correo electrónico o nombre de usuario es obligatorio")
    @JsonAlias({"correo", "email", "usuario", "username"})
    private String correo;

    @Schema(description = "Contraseña de la cuenta", example = "ContrasenaSegura123")
    @NotBlank(message = "La contraseña es obligatoria")
    @JsonAlias({"contrasena", "contraseña", "password"})
    private String contrasena;
}
