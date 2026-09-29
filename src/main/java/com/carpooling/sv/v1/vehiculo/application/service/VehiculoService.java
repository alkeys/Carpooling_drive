package com.carpooling.sv.v1.vehiculo.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.shared.exception.DuplicateResourceException;
import com.carpooling.sv.v1.shared.exception.ForbiddenException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.vehiculo.application.port.in.ActualizarVehiculoCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.DarDeBajaVehiculoCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.GetVehiculoByIdCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.GetVehiculosPorPropietarioCase;
import com.carpooling.sv.v1.vehiculo.application.port.in.RegistrarVehiculoCase;
import com.carpooling.sv.v1.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;

@Service
@Transactional
public class VehiculoService implements
        RegistrarVehiculoCase,
        GetVehiculosPorPropietarioCase,
        GetVehiculoByIdCase,
        ActualizarVehiculoCase,
        DarDeBajaVehiculoCase {

    private final VehiculoRepositoryPort vehiculoRepositoryPort;
    private final SecurityContextHelper securityHelper;

    public VehiculoService(VehiculoRepositoryPort vehiculoRepositoryPort, SecurityContextHelper securityHelper) {
        this.vehiculoRepositoryPort = vehiculoRepositoryPort;
        this.securityHelper = securityHelper;
    }

    @Override
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Validar que el usuario sea conductor o admin
        if (!securityHelper.hasRole("CONDUCTOR") && !securityHelper.isAdmin()) {
            throw new ForbiddenException("Debes tener el rol de CONDUCTOR para registrar vehículos");
        }

        // Asignar el propietario al usuario autenticado para evitar spoofing
        Vehiculo vehiculoConPropietario = new Vehiculo(
            null,
            currentUserId,
            vehiculo.marca(),
            vehiculo.modelo(),
            vehiculo.anio(),
            vehiculo.color(),
            vehiculo.tipo(),
            vehiculo.placa().trim().toUpperCase(),
            vehiculo.capacidadPasajeros(),
            vehiculo.fotoUrl(),
            true,
            null,
            null
        );

        if (vehiculoRepositoryPort.existsByPlaca(vehiculoConPropietario.placa())) {
            throw new DuplicateResourceException("Ya existe un vehículo registrado con la placa: " + vehiculoConPropietario.placa());
        }

        return vehiculoRepositoryPort.save(vehiculoConPropietario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehiculo> getVehiculosPorPropietario(UUID propietarioId) {
        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el propio conductor o un ADMIN pueden listar sus vehículos
        if (!propietarioId.equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para ver los vehículos de otro usuario");
        }

        return vehiculoRepositoryPort.findByPropietarioIdAndActivoTrue(propietarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehiculo getVehiculoById(UUID id) {
        Vehiculo vehiculo = vehiculoRepositoryPort.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el propietario o un ADMIN pueden ver el detalle privado del vehículo
        if (!vehiculo.propietarioId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para consultar este vehículo");
        }

        return vehiculo;
    }

    @Override
    public Vehiculo actualizarVehiculo(
            UUID id,
            String nuevaMarca,
            String nuevoModelo,
            Integer nuevoAnio,
            String nuevoColor,
            String nuevoTipo,
            String nuevaPlaca,
            Integer nuevaCapacidad,
            String nuevaFotoUrl) {
        Vehiculo vehiculoExistente = vehiculoRepositoryPort.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el propietario o ADMIN pueden actualizar el vehículo
        if (!vehiculoExistente.propietarioId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para modificar este vehículo");
        }

        String placaFinal = nuevaPlaca != null && !nuevaPlaca.isBlank()
            ? nuevaPlaca.trim().toUpperCase()
            : vehiculoExistente.placa();

        if (!placaFinal.equalsIgnoreCase(vehiculoExistente.placa())
                && vehiculoRepositoryPort.existsByPlacaAndIdNot(placaFinal, id)) {
            throw new DuplicateResourceException("La placa " + placaFinal + " ya está registrada por otro vehículo");
        }

        com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo tipoFinal = nuevoTipo != null && !nuevoTipo.isBlank()
            ? com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo.fromString(nuevoTipo)
            : vehiculoExistente.tipo();

        Vehiculo actualizado = vehiculoExistente.withUpdatedData(
            nuevaMarca,
            nuevoModelo,
            nuevoAnio,
            nuevoColor,
            tipoFinal,
            placaFinal,
            nuevaCapacidad,
            nuevaFotoUrl
        );

        return vehiculoRepositoryPort.update(actualizado);
    }

    @Override
    public void darDeBajaVehiculo(UUID id) {
        Vehiculo vehiculoExistente = vehiculoRepositoryPort.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        UUID currentUserId = securityHelper.getCurrentUserId();

        // Anti-IDOR: Solo el propietario o ADMIN pueden dar de baja el vehículo
        if (!vehiculoExistente.propietarioId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new ForbiddenException("No tienes permiso para dar de baja este vehículo");
        }

        Vehiculo dadoDeBaja = vehiculoExistente.withActivo(false);
        vehiculoRepositoryPort.update(dadoDeBaja);
    }
}
