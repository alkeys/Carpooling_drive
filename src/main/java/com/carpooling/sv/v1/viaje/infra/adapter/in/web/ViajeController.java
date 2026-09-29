package com.carpooling.sv.v1.viaje.infra.adapter.in.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.viaje.application.port.in.ActualizarViajeCase;
import com.carpooling.sv.v1.viaje.application.port.in.AgregarPuntoEncuentroCase;
import com.carpooling.sv.v1.viaje.application.port.in.BuscarViajesCase;
import com.carpooling.sv.v1.viaje.application.port.in.CambiarEstadoViajeCase;
import com.carpooling.sv.v1.viaje.application.port.in.GetViajeByIdCase;
import com.carpooling.sv.v1.viaje.application.port.in.GetViajesPorConductorCase;
import com.carpooling.sv.v1.viaje.application.port.in.PublicarViajeCase;
import com.carpooling.sv.v1.viaje.domain.PuntoEncuentro;
import com.carpooling.sv.v1.viaje.domain.Viaje;
import com.carpooling.sv.v1.viaje.domain.value.Coordenada;
import com.carpooling.sv.v1.viaje.domain.value.EstadoViaje;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.PuntoEncuentroRequest;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.PuntoEncuentroResponse;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.ViajePublicarRequest;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.ViajeResponse;
import com.carpooling.sv.v1.viaje.infra.adapter.in.web.dto.ViajeUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/viajes")
@Tag(name = "Viajes", description = "Publicación, búsqueda geoespacial y gestión del ciclo de vida de viajes")
public class ViajeController {

    private final PublicarViajeCase publicarViajeCase;
    private final GetViajeByIdCase getViajeByIdCase;
    private final BuscarViajesCase buscarViajesCase;
    private final GetViajesPorConductorCase getViajesPorConductorCase;
    private final ActualizarViajeCase actualizarViajeCase;
    private final CambiarEstadoViajeCase cambiarEstadoViajeCase;
    private final AgregarPuntoEncuentroCase agregarPuntoEncuentroCase;
    private final SecurityContextHelper securityHelper;

    public ViajeController(
            PublicarViajeCase publicarViajeCase,
            GetViajeByIdCase getViajeByIdCase,
            BuscarViajesCase buscarViajesCase,
            GetViajesPorConductorCase getViajesPorConductorCase,
            ActualizarViajeCase actualizarViajeCase,
            CambiarEstadoViajeCase cambiarEstadoViajeCase,
            AgregarPuntoEncuentroCase agregarPuntoEncuentroCase,
            SecurityContextHelper securityHelper) {
        this.publicarViajeCase = publicarViajeCase;
        this.getViajeByIdCase = getViajeByIdCase;
        this.buscarViajesCase = buscarViajesCase;
        this.getViajesPorConductorCase = getViajesPorConductorCase;
        this.actualizarViajeCase = actualizarViajeCase;
        this.cambiarEstadoViajeCase = cambiarEstadoViajeCase;
        this.agregarPuntoEncuentroCase = agregarPuntoEncuentroCase;
        this.securityHelper = securityHelper;
    }

    @Operation(summary = "Publicar un viaje", description = "Permite a un conductor publicar un nuevo viaje con fecha, hora, cupos y puntos de encuentro. Requiere rol CONDUCTOR o ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Viaje publicado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Vehículo ajeno o no es CONDUCTOR)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public ViajeResponse publicarViaje(@Valid @RequestBody ViajePublicarRequest request) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        Viaje viaje = new Viaje(
            null,
            currentUserId,
            request.getVehiculoId(),
            request.getRutaId(),
            request.getOrigenNombre(),
            new Coordenada(request.getOrigenLat(), request.getOrigenLon()),
            request.getDestinoNombre(),
            new Coordenada(request.getDestinoLat(), request.getDestinoLon()),
            request.getFechaSalida(),
            request.getHoraSalida(),
            request.getCuposTotales(),
            request.getCuposTotales(),
            request.getTarifaAproximada(),
            request.getDescripcion(),
            EstadoViaje.PUBLICADO,
            List.of(),
            null,
            null
        );

        List<PuntoEncuentro> puntos = new ArrayList<>();
        if (request.getPuntosEncuentro() != null) {
            for (PuntoEncuentroRequest pr : request.getPuntosEncuentro()) {
                puntos.add(new PuntoEncuentro(
                    null,
                    null,
                    pr.getNombre(),
                    pr.getDescripcion(),
                    new Coordenada(pr.getLatitud(), pr.getLongitud()),
                    pr.getOrden(),
                    pr.getPermiteSubida() != null ? pr.getPermiteSubida() : true,
                    pr.getPermiteBajada() != null ? pr.getPermiteBajada() : true,
                    null
                ));
            }
        }

        Viaje creado = publicarViajeCase.publicarViaje(viaje, puntos);
        return toResponse(creado);
    }

    @Operation(summary = "Buscar viajes (Público)", description = "Búsqueda geoespacial por coordenadas de origen y destino con radio de proximidad (km), o por texto libre, fecha y cupos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultados de búsqueda recuperados exitosamente")
    })
    @GetMapping("/buscar")
    public List<ViajeResponse> buscarViajes(
            @RequestParam(required = false) Double origenLat,
            @RequestParam(required = false) Double origenLon,
            @RequestParam(required = false, defaultValue = "10.0") Double radioOrigenKm,
            @RequestParam(required = false) Double destinoLat,
            @RequestParam(required = false) Double destinoLon,
            @RequestParam(required = false, defaultValue = "10.0") Double radioDestinoKm,
            @RequestParam(required = false) String origenTexto,
            @RequestParam(required = false) String destinoTexto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false, defaultValue = "1") Integer cuposMinimos) {
        return buscarViajesCase.buscarViajes(
            origenLat, origenLon, radioOrigenKm,
            destinoLat, destinoLon, radioDestinoKm,
            origenTexto, destinoTexto, fecha, cuposMinimos
        ).stream()
            .map(this::toResponse)
            .toList();
    }

    @Operation(summary = "Obtener viaje por ID (Público)", description = "Obtiene los detalles completos de un viaje publicado y sus paradas intermedias.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Viaje encontrado"),
        @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    })
    @GetMapping("/{id}")
    public ViajeResponse getViajeById(@PathVariable UUID id) {
        return toResponse(getViajeByIdCase.getViajeById(id));
    }

    @Operation(summary = "Listar mis viajes publicados", description = "Devuelve el historial de viajes publicados por el conductor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de viajes recuperada"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/mis-viajes")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public List<ViajeResponse> getMisViajes() {
        UUID currentUserId = securityHelper.getCurrentUserId();
        return getViajesPorConductorCase.getViajesPorConductor(currentUserId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Operation(summary = "Actualizar viaje", description = "Modifica los datos de un viaje en estado PUBLICADO. Protección anti-IDOR: solo el conductor creador o ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Viaje actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No eres el conductor de este viaje)"),
        @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public ViajeResponse actualizarViaje(
            @PathVariable UUID id,
            @Valid @RequestBody ViajeUpdateRequest request) {
        Coordenada origenCoord = (request.getOrigenLat() != null && request.getOrigenLon() != null)
            ? new Coordenada(request.getOrigenLat(), request.getOrigenLon())
            : null;

        Coordenada destinoCoord = (request.getDestinoLat() != null && request.getDestinoLon() != null)
            ? new Coordenada(request.getDestinoLat(), request.getDestinoLon())
            : null;

        Viaje actualizado = actualizarViajeCase.actualizarViaje(
            id,
            request.getOrigenNombre(),
            origenCoord,
            request.getDestinoNombre(),
            destinoCoord,
            request.getFechaSalida(),
            request.getHoraSalida(),
            request.getTarifaAproximada(),
            request.getDescripcion()
        );
        return toResponse(actualizado);
    }

    @Operation(summary = "Cambiar estado de viaje", description = "Permite al conductor cambiar el estado (EN_CURSO, FINALIZADO, CANCELADO). Protección anti-IDOR: solo el creador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No eres el conductor)"),
        @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    })
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public ViajeResponse cambiarEstadoViaje(
            @PathVariable UUID id,
            @RequestParam String estado) {
        EstadoViaje nuevoEstado = EstadoViaje.fromString(estado);
        return toResponse(cambiarEstadoViajeCase.cambiarEstadoViaje(id, nuevoEstado));
    }

    @Operation(summary = "Agregar parada intermedia", description = "Agrega un nuevo punto de encuentro a un viaje en estado PUBLICADO. Protección anti-IDOR.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Punto de encuentro añadido exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    })
    @PostMapping("/{id}/puntos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public PuntoEncuentroResponse agregarPuntoEncuentro(
            @PathVariable UUID id,
            @Valid @RequestBody PuntoEncuentroRequest request) {
        PuntoEncuentro punto = new PuntoEncuentro(
            null,
            id,
            request.getNombre(),
            request.getDescripcion(),
            new Coordenada(request.getLatitud(), request.getLongitud()),
            request.getOrden(),
            request.getPermiteSubida() != null ? request.getPermiteSubida() : true,
            request.getPermiteBajada() != null ? request.getPermiteBajada() : true,
            null
        );
        PuntoEncuentro guardado = agregarPuntoEncuentroCase.agregarPuntoEncuentro(id, punto);
        return toPuntoResponse(guardado);
    }

    private ViajeResponse toResponse(Viaje v) {
        List<PuntoEncuentroResponse> puntosResp = v.puntosEncuentro() != null
            ? v.puntosEncuentro().stream().map(this::toPuntoResponse).toList()
            : List.of();

        return new ViajeResponse(
            v.id() != null ? v.id().toString() : null,
            v.conductorId() != null ? v.conductorId().toString() : null,
            v.vehiculoId() != null ? v.vehiculoId().toString() : null,
            v.rutaId() != null ? v.rutaId().toString() : null,
            v.origenNombre(),
            v.origenCoordenada().latitud(),
            v.origenCoordenada().longitud(),
            v.destinoNombre(),
            v.destinoCoordenada().latitud(),
            v.destinoCoordenada().longitud(),
            v.fechaSalida() != null ? v.fechaSalida().toString() : null,
            v.horaSalida() != null ? v.horaSalida().toString() : null,
            v.cuposTotales(),
            v.cuposDisponibles(),
            v.tarifaAproximada(),
            v.descripcion(),
            v.estado() != null ? v.estado().name() : "PUBLICADO",
            puntosResp,
            v.creadoEn() != null ? v.creadoEn().toString() : null,
            v.actualizadoEn() != null ? v.actualizadoEn().toString() : null
        );
    }

    private PuntoEncuentroResponse toPuntoResponse(PuntoEncuentro p) {
        return new PuntoEncuentroResponse(
            p.id() != null ? p.id().toString() : null,
            p.viajeId() != null ? p.viajeId().toString() : null,
            p.nombre(),
            p.descripcion(),
            p.ubicacion().latitud(),
            p.ubicacion().longitud(),
            p.orden(),
            p.permiteSubida(),
            p.permiteBajada()
        );
    }
}
