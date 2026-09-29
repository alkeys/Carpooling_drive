package com.carpooling.sv.v1.ruta.infra.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar una ruta en el catálogo del sistema")
public class RutaRequest {

    @NotBlank(message = "El nombre de la ruta es obligatorio")
    @Size(max = 150, message = "El nombre de la ruta no puede exceder 150 caracteres")
    @Schema(description = "Nombre descriptivo de la ruta", example = "Santa Ana -> San Salvador por Panamericana")
    private String nombre;

    @Schema(description = "Descripción detallada de la ruta, paradas usuales o referencias", example = "Ruta directa por Carretera Panamericana pasando por El Congo, Ciudad Arce y Los Chorros")
    private String descripcion;

    public RutaRequest(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
