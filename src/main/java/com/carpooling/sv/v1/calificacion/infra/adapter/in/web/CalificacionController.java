package com.carpooling.sv.v1.calificacion.infra.adapter.in.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.calificacion.application.port.in.CalificarUsuarioCase;
import com.carpooling.sv.v1.calificacion.application.port.in.GetCalificacionesPorUsuarioCase;
import com.carpooling.sv.v1.calificacion.application.port.in.GetReputacionUsuarioCase;
import com.carpooling.sv.v1.calificacion.domain.Calificacion;
import com.carpooling.sv.v1.calificacion.domain.ReputacionUsuario;
import com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto.CalificacionResponse;
import com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto.CalificarRequest;
import com.carpooling.sv.v1.calificacion.infra.adapter.in.web.dto.ReputacionResponse;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/calificaciones")
@Tag(name = "Calificaciones y Reseñas", description = "Sistema de reputación mutua post-viaje entre conductores y pasajeros")
public class CalificacionController {

    private final CalificarUsuarioCase calificarUsuarioCase;
    private final GetCalificacionesPorUsuarioCase getCalificacionesPorUsuarioCase;
    private final GetReputacionUsuarioCase getReputacionUsuarioCase;
    private final SecurityContextHelper securityHelper;

    public CalificacionController(
            CalificarUsuarioCase calificarUsuarioCase,
            GetCalificacionesPorUsuarioCase getCalificacionesPorUsuarioCase,
            GetReputacionUsuarioCase getReputacionUsuarioCase,
            SecurityContextHelper securityHelper) {
        this.calificarUsuarioCase = calificarUsuarioCase;
        this.getCalificacionesPorUsuarioCase = getCalificacionesPorUsuarioCase;
        this.getReputacionUsuarioCase = getReputacionUsuarioCase;
        this.securityHelper = securityHelper;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Calificar a un usuario post-viaje", description = "Permite calificar a otro usuario (conductor a pasajero o pasajero a conductor) tras haber completado un viaje.")
    @ApiResponse(responseCode = "201", description = "Calificación registrada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, autocalificación o el viaje no ha finalizado")
    @ApiResponse(responseCode = "403", description = "No participaste en este viaje con dicho usuario")
    @ApiResponse(responseCode = "409", description = "Ya calificaste a este usuario en este viaje")
    public ResponseEntity<CalificacionResponse> calificar(@Valid @RequestBody CalificarRequest request) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Calificacion comando = new Calificacion(
            null,
            request.viajeId(),
            currentUserId,
            request.evaluadoId(),
            request.puntuacion(),
            request.comentario(),
            null
        );

        Calificacion creada = calificarUsuarioCase.calificarUsuario(comando);
        return ResponseEntity.created(URI.create("/api/v1/calificaciones/" + creada.id()))
            .body(CalificacionResponse.fromDomain(creada));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Consultar reputación pública de un usuario", description = "Obtiene el promedio de estrellas, total de opiniones y últimas reseñas del usuario. Endpoint público sin autenticación requerida.")
    @ApiResponse(responseCode = "200", description = "Reputación del usuario")
    public ResponseEntity<ReputacionResponse> getReputacionUsuario(@PathVariable UUID usuarioId) {
        ReputacionUsuario reputacion = getReputacionUsuarioCase.getReputacionPorUsuario(usuarioId);
        return ResponseEntity.ok(ReputacionResponse.fromDomain(reputacion));
    }

    @GetMapping("/usuario/{usuarioId}/todas")
    @Operation(summary = "Listar todas las reseñas de un usuario", description = "Lista completa de todas las calificaciones recibidas por el usuario. Endpoint público.")
    @ApiResponse(responseCode = "200", description = "Lista de calificaciones")
    public ResponseEntity<List<CalificacionResponse>> getTodasLasCalificaciones(@PathVariable UUID usuarioId) {
        List<Calificacion> calificaciones = getCalificacionesPorUsuarioCase.getCalificacionesPorUsuario(usuarioId);
        List<CalificacionResponse> response = calificaciones.stream()
            .map(CalificacionResponse::fromDomain)
            .toList();
        return ResponseEntity.ok(response);
    }
}
