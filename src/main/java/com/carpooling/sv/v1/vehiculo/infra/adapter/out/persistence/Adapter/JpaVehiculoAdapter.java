package com.carpooling.sv.v1.vehiculo.infra.adapter.out.persistence.Adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.carpooling.sv.v1.vehiculo.domain.Vehiculo;
import com.carpooling.sv.v1.vehiculo.domain.value.TipoVehiculo;
import com.carpooling.sv.v1.vehiculo.infra.adapter.out.persistence.Jpa.SpringDataVehiculoRepository;
import com.carpooling.sv.v1.vehiculo.infra.adapter.out.persistence.entity.JpaVehiculo;

@Component
public class JpaVehiculoAdapter implements VehiculoRepositoryPort {

    private final SpringDataVehiculoRepository repository;

    public JpaVehiculoAdapter(SpringDataVehiculoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Vehiculo save(Vehiculo vehiculo) {
        JpaVehiculo jpa = toJpaEntity(vehiculo);
        JpaVehiculo saved = repository.save(jpa);
        return toDomain(saved);
    }

    @Override
    public Optional<Vehiculo> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Vehiculo> findByIdAndActivoTrue(UUID id) {
        return repository.findByIdAndActivoTrue(id).map(this::toDomain);
    }

    @Override
    public List<Vehiculo> findByPropietarioIdAndActivoTrue(UUID propietarioId) {
        return repository.findByPropietarioIdAndActivoTrue(propietarioId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return repository.existsByPlacaIgnoreCase(placa != null ? placa.trim() : "");
    }

    @Override
    public boolean existsByPlacaAndIdNot(String placa, UUID id) {
        return repository.existsByPlacaIgnoreCaseAndIdNot(placa != null ? placa.trim() : "", id);
    }

    @Override
    public Vehiculo update(Vehiculo vehiculo) {
        JpaVehiculo jpa = toJpaEntity(vehiculo);
        JpaVehiculo updated = repository.save(jpa);
        return toDomain(updated);
    }

    private JpaVehiculo toJpaEntity(Vehiculo d) {
        return new JpaVehiculo(
            d.id(),
            d.propietarioId(),
            d.marca(),
            d.modelo(),
            d.anio(),
            d.color(),
            d.tipo() != null ? d.tipo().name() : "SEDAN",
            d.placa(),
            (short) d.capacidadPasajeros(),
            d.fotoUrl(),
            d.activo(),
            d.creadoEn(),
            d.actualizadoEn()
        );
    }

    private Vehiculo toDomain(JpaVehiculo j) {
        return new Vehiculo(
            j.getId(),
            j.getPropietarioId(),
            j.getMarca(),
            j.getModelo(),
            j.getAnio(),
            j.getColor(),
            TipoVehiculo.fromString(j.getTipo()),
            j.getPlaca(),
            j.getCapacidadPasajeros(),
            j.getFotoUrl(),
            j.isActivo(),
            j.getCreadoEn(),
            j.getActualizadoEn()
        );
    }
}
