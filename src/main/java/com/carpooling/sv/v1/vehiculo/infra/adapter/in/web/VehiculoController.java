package com.carpooling.sv.v1.vehiculo.infra.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.vehiculo.application.port.in.ActualizarVehiculoCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.DarDeBajaVehiculoCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.GetVehiculoByIdCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.GetVehiculosPorPropietarioCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.RegistrarVehiculoCase;
import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;
import com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo;
import com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto.VehiculoRequest;
import com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto.VehiculoResponse;
import com.carpooling.sv.v1.vehiculo.infra.adapter.in.web.dto.VehiculoUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehículos", description = "Gestión de vehículos registrados por conductores para carpooling")
public class VehiculoController {

    private final RegistrarVehiculoCase registrarVehiculoCase;
    private final GetVehiculosPorPropietarioCase getVehiculosPorPropietarioCase;
    private final GetVehiculoByIdCase getVehiculoByIdCase;
    private final ActualizarVehiculoCase actualizarVehiculoCase;
    private final DarDeBajaVehiculoCase darDeBajaVehiculoCase;
    private final SecurityContextHelper securityHelper;

    public VehiculoController(
            RegistrarVehiculoCase registrarVehiculoCase,
            GetVehiculosPorPropietarioCase getVehiculosPorPropietarioCase,
            GetVehiculoByIdCase getVehiculoByIdCase,
            ActualizarVehiculoCase actualizarVehiculoCase,
            DarDeBajaVehiculoCase darDeBajaVehiculoCase,
            SecurityContextHelper securityHelper) {
        this.registrarVehiculoCase = registrarVehiculoCase;
        this.getVehiculosPorPropietarioCase = getVehiculosPorPropietarioCase;
        this.getVehiculoByIdCase = getVehiculoByIdCase;
        this.actualizarVehiculoCase = actualizarVehiculoCase;
        this.darDeBajaVehiculoCase = darDeBajaVehiculoCase;
        this.securityHelper = securityHelper;
    }

    @Operation(summary = "Registrar un vehículo", description = "Permite a un conductor registrar un vehículo en la plataforma. Requiere rol CONDUCTOR o ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehículo registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol CONDUCTOR)"),
        @ApiResponse(responseCode = "409", description = "La placa ya se encuentra registrada")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public VehiculoResponse registrarVehiculo(@Valid @RequestBody VehiculoRequest request) {
        UUID currentUserId = securityHelper.getCurrentUserId();
        Vehiculo vehiculo = new Vehiculo(
            null,
            currentUserId,
            request.getMarca(),
            request.getModelo(),
            request.getAnio(),
            request.getColor(),
            TipoVehiculo.fromString(request.getTipo()),
            request.getPlaca(),
            request.getCapacidadPasajeros(),
            request.getFotoUrl(),
            true,
            null,
            null
        );
        Vehiculo creado = registrarVehiculoCase.registrarVehiculo(vehiculo);
        return toResponse(creado);
    }

    @Operation(summary = "Listar mis vehículos", description = "Devuelve la lista de vehículos activos registrados por el conductor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de vehículos obtenida"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/mis-vehiculos")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public List<VehiculoResponse> getMisVehiculos() {
        UUID currentUserId = securityHelper.getCurrentUserId();
        return getVehiculosPorPropietarioCase.getVehiculosPorPropietario(currentUserId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Operation(summary = "Obtener vehículo por ID", description = "Consulta el detalle de un vehículo específico. Protección anti-IDOR: solo el propietario o ADMIN pueden consultarlo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No eres el propietario)"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public VehiculoResponse getVehiculoById(@PathVariable UUID id) {
        return toResponse(getVehiculoByIdCase.getVehiculoById(id));
    }

    @Operation(summary = "Actualizar vehículo", description = "Modifica los datos de un vehículo existente. Protección anti-IDOR: solo el propietario o ADMIN pueden actualizarlo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No eres el propietario)"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con la nueva placa")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public VehiculoResponse actualizarVehiculo(
            @PathVariable UUID id,
            @Valid @RequestBody VehiculoUpdateRequest request) {
        Vehiculo actualizado = actualizarVehiculoCase.actualizarVehiculo(
            id,
            request.getMarca(),
            request.getModelo(),
            request.getAnio(),
            request.getColor(),
            request.getTipo(),
            request.getPlaca(),
            request.getCapacidadPasajeros(),
            request.getFotoUrl()
        );
        return toResponse(actualizado);
    }

    @Operation(summary = "Dar de baja un vehículo", description = "Desactiva lógicamente el vehículo. Protección anti-IDOR: solo el propietario o ADMIN pueden darlo de baja.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Vehículo dado de baja exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No eres el propietario)"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMIN')")
    public void darDeBajaVehiculo(@PathVariable UUID id) {
        darDeBajaVehiculoCase.darDeBajaVehiculo(id);
    }

    private VehiculoResponse toResponse(Vehiculo v) {
        return new VehiculoResponse(
            v.id() != null ? v.id().toString() : null,
            v.propietarioId() != null ? v.propietarioId().toString() : null,
            v.marca(),
            v.modelo(),
            v.anio(),
            v.color(),
            v.tipo() != null ? v.tipo().name() : "SEDAN",
            v.placa(),
            v.capacidadPasajeros(),
            v.fotoUrl(),
            v.activo(),
            v.creadoEn() != null ? v.creadoEn().toString() : null,
            v.actualizadoEn() != null ? v.actualizadoEn().toString() : null
        );
    }
}
