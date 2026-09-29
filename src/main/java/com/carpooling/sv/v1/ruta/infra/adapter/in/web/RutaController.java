package com.carpooling.sv.v1.ruta.infra.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.ruta.application.port.in.ActualizarRutaCase;
import com.carpooling.sv.v1.ruta.application.port.in.CambiarEstadoRutaCase;
import com.carpooling.sv.v1.ruta.application.port.in.CrearRutaCase;
import com.carpooling.sv.v1.ruta.application.port.in.GetAllRutasCase;
import com.carpooling.sv.v1.ruta.application.port.in.GetRutaByIdCase;
import com.carpooling.sv.v1.ruta.application.port.in.GetRutasActivasCase;
import com.carpooling.sv.v1.ruta.domain.Ruta;
import com.carpooling.sv.v1.ruta.infra.adapter.in.web.dto.RutaRequest;
import com.carpooling.sv.v1.ruta.infra.adapter.in.web.dto.RutaResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rutas")
@Tag(name = "Rutas", description = "Catálogo de rutas del sistema para publicación y exploración de viajes")
public class RutaController {

    private final CrearRutaCase crearRutaCase;
    private final GetRutaByIdCase getRutaByIdCase;
    private final GetRutasActivasCase getRutasActivasCase;
    private final GetAllRutasCase getAllRutasCase;
    private final ActualizarRutaCase actualizarRutaCase;
    private final CambiarEstadoRutaCase cambiarEstadoRutaCase;

    public RutaController(
            CrearRutaCase crearRutaCase,
            GetRutaByIdCase getRutaByIdCase,
            GetRutasActivasCase getRutasActivasCase,
            GetAllRutasCase getAllRutasCase,
            ActualizarRutaCase actualizarRutaCase,
            CambiarEstadoRutaCase cambiarEstadoRutaCase) {
        this.crearRutaCase = crearRutaCase;
        this.getRutaByIdCase = getRutaByIdCase;
        this.getRutasActivasCase = getRutasActivasCase;
        this.getAllRutasCase = getAllRutasCase;
        this.actualizarRutaCase = actualizarRutaCase;
        this.cambiarEstadoRutaCase = cambiarEstadoRutaCase;
    }

    @Operation(summary = "Listar rutas activas (Público)", description = "Permite a cualquier cliente, conductor o visitante consultar el catálogo de rutas activas disponibles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de rutas activas recuperada exitosamente")
    })
    @GetMapping
    public List<RutaResponse> getRutasActivas() {
        return getRutasActivasCase.getRutasActivas().stream()
            .map(this::toResponse)
            .toList();
    }

    @Operation(summary = "Listar todas las rutas (Admin)", description = "Permite a un administrador ver todas las rutas, incluyendo inactivas. Requiere rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista completa recuperada"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)")
    })
    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RutaResponse> getAllRutas() {
        return getAllRutasCase.getAllRutas().stream()
            .map(this::toResponse)
            .toList();
    }

    @Operation(summary = "Obtener ruta por ID (Público)", description = "Consulta los detalles de una ruta específica.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ruta encontrada"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    @GetMapping("/{id}")
    public RutaResponse getRutaById(@PathVariable UUID id) {
        return toResponse(getRutaByIdCase.getRutaById(id));
    }

    @Operation(summary = "Crear nueva ruta (Admin)", description = "Agrega una nueva ruta al catálogo oficial. Requiere rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ruta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)"),
        @ApiResponse(responseCode = "409", description = "Ya existe una ruta con ese nombre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RutaResponse crearRuta(@Valid @RequestBody RutaRequest request) {
        Ruta ruta = new Ruta(request.getNombre(), request.getDescripcion());
        return toResponse(crearRutaCase.crearRuta(ruta));
    }

    @Operation(summary = "Actualizar ruta (Admin)", description = "Modifica los datos de una ruta existente. Requiere rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ruta actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto con el nombre de ruta")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RutaResponse actualizarRuta(
            @PathVariable UUID id,
            @Valid @RequestBody RutaRequest request) {
        Ruta modificada = new Ruta(id, request.getNombre(), request.getDescripcion(), true, null, null);
        return toResponse(actualizarRutaCase.actualizarRuta(id, modificada));
    }

    @Operation(summary = "Cambiar estado de ruta (Admin)", description = "Activa o desactiva una ruta en el catálogo. Requiere rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public RutaResponse cambiarEstadoRuta(
            @PathVariable UUID id,
            @RequestParam boolean activo) {
        return toResponse(cambiarEstadoRutaCase.cambiarEstadoRuta(id, activo));
    }

    private RutaResponse toResponse(Ruta r) {
        return new RutaResponse(
            r.id() != null ? r.id().toString() : null,
            r.nombre(),
            r.descripcion(),
            r.activo(),
            r.creadoEn() != null ? r.creadoEn().toString() : null,
            r.actualizadoEn() != null ? r.actualizadoEn().toString() : null
        );
    }
}
