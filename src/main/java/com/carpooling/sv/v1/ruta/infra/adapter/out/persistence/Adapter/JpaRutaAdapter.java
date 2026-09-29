package com.carpooling.sv.v1.ruta.infra.adapter.out.persistence.Adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.ruta.application.port.out.RutaRepositoryPort;
import com.carpooling.sv.v1.ruta.domain.Ruta;
import com.carpooling.sv.v1.ruta.infra.adapter.out.persistence.Jpa.SpringDataRutaRepository;
import com.carpooling.sv.v1.ruta.infra.adapter.out.persistence.entity.JpaRuta;

@Component
public class JpaRutaAdapter implements RutaRepositoryPort {

    private final SpringDataRutaRepository repository;

    public JpaRutaAdapter(SpringDataRutaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Ruta save(Ruta ruta) {
        JpaRuta jpa = toJpaEntity(ruta);
        JpaRuta saved = repository.save(jpa);
        return toDomain(saved);
    }

    @Override
    public Optional<Ruta> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Ruta> findByIdAndActivoTrue(UUID id) {
        return repository.findByIdAndActivoTrue(id).map(this::toDomain);
    }

    @Override
    public List<Ruta> findAll() {
        return repository.findAll().stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<Ruta> findByActivoTrue() {
        return repository.findByActivoTrue().stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return repository.existsByNombreIgnoreCase(nombre != null ? nombre.trim() : "");
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, UUID id) {
        return repository.existsByNombreIgnoreCaseAndIdNot(nombre != null ? nombre.trim() : "", id);
    }

    @Override
    public Ruta update(Ruta ruta) {
        JpaRuta jpa = toJpaEntity(ruta);
        JpaRuta updated = repository.save(jpa);
        return toDomain(updated);
    }

    private JpaRuta toJpaEntity(Ruta r) {
        return new JpaRuta(
            r.id(),
            r.nombre(),
            r.descripcion(),
            r.activo(),
            r.creadoEn(),
            r.actualizadoEn()
        );
    }

    private Ruta toDomain(JpaRuta j) {
        return new Ruta(
            j.getId(),
            j.getNombre(),
            j.getDescripcion(),
            j.isActivo(),
            j.getCreadoEn(),
            j.getActualizadoEn()
        );
    }
}
