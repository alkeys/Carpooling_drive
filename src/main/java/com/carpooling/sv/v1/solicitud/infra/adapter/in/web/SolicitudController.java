package com.carpooling.sv.v1.solicitud.infra.adapter.in.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.solicitud.application.port.in.CancelarSolicitudCase;
import com.carpooling.sv.v1.solicitud.application.port.in.GetSolicitudByIdCase;
import com.carpooling.sv.v1.solicitud.application.port.in.GetSolicitudesPorPasajeroCase;
import com.carpooling.sv.v1.solicitud.application.port.in.GetSolicitudesPorViajeCase;
import com.carpooling.sv.v1.solicitud.application.port.in.ResponderSolicitudCase;
import com.carpooling.sv.v1.solicitud.application.port.in.SolicitarCupoCase;
import com.carpooling.sv.v1.solicitud.domain.SolicitudViaje;
import com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto.ResponderSolicitudRequest;
import com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto.SolicitudCrearRequest;
import com.carpooling.sv.v1.solicitud.infra.adapter.in.web.dto.SolicitudResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/solicitudes-viaje")
@Tag(name = "Solicitudes de Viaje", description = "Gestión de reservas de cupos, confirmación por conductor y cancelaciones")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudController {

    private final SolicitarCupoCase solicitarCupoCase;
    private final GetSolicitudesPorPasajeroCase getSolicitudesPorPasajeroCase;
    private final GetSolicitudesPorViajeCase getSolicitudesPorViajeCase;
    private final GetSolicitudByIdCase getSolicitudByIdCase;
    private final ResponderSolicitudCase responderSolicitudCase;
    private final CancelarSolicitudCase cancelarSolicitudCase;
    private final SecurityContextHelper securityHelper;

    public SolicitudController(
            SolicitarCupoCase solicitarCupoCase,
            GetSolicitudesPorPasajeroCase getSolicitudesPorPasajeroCase,
            GetSolicitudesPorViajeCase getSolicitudesPorViajeCase,
            GetSolicitudByIdCase getSolicitudByIdCase,
            ResponderSolicitudCase responderSolicitudCase,
            CancelarSolicitudCase cancelarSolicitudCase,
            SecurityContextHelper securityHelper) {
        this.solicitarCupoCase = solicitarCupoCase;
        this.getSolicitudesPorPasajeroCase = getSolicitudesPorPasajeroCase;
        this.getSolicitudesPorViajeCase = getSolicitudesPorViajeCase;
        this.getSolicitudByIdCase = getSolicitudByIdCase;
        this.responderSolicitudCase = responderSolicitudCase;
        this.cancelarSolicitudCase = cancelarSolicitudCase;
        this.securityHelper = securityHelper;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Solicitar cupo en un viaje", description = "El pasajero autenticado solicita reservar uno o más cupos en un viaje publicado.")
    @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente en estado PENDIENTE")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o no hay cupos suficientes")
    @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    @ApiResponse(responseCode = "409", description = "Ya existe una solicitud activa para este viaje")
    public ResponseEntity<SolicitudResponse> solicitarCupo(@Valid @RequestBody SolicitudCrearRequest request) {
        UUID currentUserId = securityHelper.getCurrentUserId();
        SolicitudViaje comando = new SolicitudViaje(
            null,
            request.viajeId(),
            currentUserId,
            request.puntoSubidaId(),
            request.puntoBajadaId(),
            request.cantidadCupos() > 0 ? request.cantidadCupos() : 1,
            null,
            request.mensaje(),
            null,
            null,
            null
        );

        SolicitudViaje creada = solicitarCupoCase.solicitarCupo(comando);
        return ResponseEntity.created(URI.create("/api/v1/solicitudes-viaje/" + creada.id()))
            .body(SolicitudResponse.fromDomain(creada));
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mis solicitudes enviadas", description = "Obtiene todas las solicitudes de viaje enviadas por el pasajero autenticado.")
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes del pasajero")
    public ResponseEntity<List<SolicitudResponse>> getMisSolicitudes() {
        UUID currentUserId = securityHelper.getCurrentUserId();
        List<SolicitudViaje> solicitudes = getSolicitudesPorPasajeroCase.getSolicitudesPorPasajero(currentUserId);
        List<SolicitudResponse> response = solicitudes.stream()
            .map(SolicitudResponse::fromDomain)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/viaje/{viajeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Solicitudes recibidas para un viaje", description = "Obtiene las solicitudes recibidas para un viaje (solo visible para el conductor del viaje o ADMIN).")
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes recibidas")
    @ApiResponse(responseCode = "403", description = "No eres el conductor de este viaje")
    @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    public ResponseEntity<List<SolicitudResponse>> getSolicitudesPorViaje(@PathVariable UUID viajeId) {
        List<SolicitudViaje> solicitudes = getSolicitudesPorViajeCase.getSolicitudesPorViaje(viajeId);
        List<SolicitudResponse> response = solicitudes.stream()
            .map(SolicitudResponse::fromDomain)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener solicitud por ID", description = "Obtiene el detalle de una solicitud (solo visible para el pasajero, el conductor del viaje o ADMIN).")
    @ApiResponse(responseCode = "200", description = "Detalle de la solicitud")
    @ApiResponse(responseCode = "403", description = "No tienes permiso para ver esta solicitud")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    public ResponseEntity<SolicitudResponse> getSolicitudById(@PathVariable UUID id) {
        SolicitudViaje solicitud = getSolicitudByIdCase.getSolicitudById(id);
        return ResponseEntity.ok(SolicitudResponse.fromDomain(solicitud));
    }

    @PatchMapping("/{id}/responder")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Responder solicitud de cupo", description = "El conductor del viaje acepta o rechaza la solicitud. Si se acepta, los cupos del viaje se descuentan automáticamente.")
    @ApiResponse(responseCode = "200", description = "Solicitud respondida exitosamente")
    @ApiResponse(responseCode = "400", description = "No hay cupos suficientes para aceptar")
    @ApiResponse(responseCode = "403", description = "Solo el conductor del viaje puede responder")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    public ResponseEntity<SolicitudResponse> responderSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody ResponderSolicitudRequest request) {
        SolicitudViaje respondida = responderSolicitudCase.responderSolicitud(id, request.aceptar());
        return ResponseEntity.ok(SolicitudResponse.fromDomain(respondida));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancelar solicitud de cupo", description = "El pasajero cancela su solicitud. Si ya estaba aceptada, los cupos se reintegran automáticamente al viaje.")
    @ApiResponse(responseCode = "200", description = "Solicitud cancelada exitosamente")
    @ApiResponse(responseCode = "403", description = "No puedes cancelar solicitudes de otro usuario")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    public ResponseEntity<SolicitudResponse> cancelarSolicitud(@PathVariable UUID id) {
        SolicitudViaje cancelada = cancelarSolicitudCase.cancelarSolicitud(id);
        return ResponseEntity.ok(SolicitudResponse.fromDomain(cancelada));
    }
}
