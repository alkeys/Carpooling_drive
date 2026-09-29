package com.carpooling.sv.v1.viaje.infra.adapter.out.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.carpooling.sv.v1.viaje.application.port.out.VehiculoVerificationPort;

@Component
public class VehiculoVerificationAdapter implements VehiculoVerificationPort {

    private final VehiculoRepositoryPort vehiculoRepositoryPort;

    public VehiculoVerificationAdapter(VehiculoRepositoryPort vehiculoRepositoryPort) {
        this.vehiculoRepositoryPort = vehiculoRepositoryPort;
    }

    @Override
    public boolean isVehiculoValidoParaConductor(UUID vehiculoId, UUID conductorId, int cuposRequeridos) {
        return vehiculoRepositoryPort.findByIdAndActivoTrue(vehiculoId)
            .map(v -> v.propietarioId().equals(conductorId) && v.capacidadPasajeros() >= cuposRequeridos)
            .orElse(false);
    }
}
