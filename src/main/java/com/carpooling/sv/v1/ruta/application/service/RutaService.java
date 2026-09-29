package com.carpooling.sv.v1.ruta.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpooling.sv.v1.ruta.application.port.in.ActualizarRutaCase;
import com.carpooling.sv.v1.ruta.application.port.in.CambiarEstadoRutaCase;
import com.carpooling.sv.v1.ruta.application.port.in.CrearRutaCase;
import com.carpooling.sv.v1.ruta.application.port.in.GetAllRutasCase;
import com.carpooling.sv.v1.ruta.application.port.in.GetRutaByIdCase;
import com.carpooling.sv.v1.ruta.application.port.in.GetRutasActivasCase;
import com.carpooling.sv.v1.ruta.application.port.out.RutaRepositoryPort;
import com.carpooling.sv.v1.ruta.domain.Ruta;
import com.carpooling.sv.v1.shared.exception.DuplicateResourceException;
import com.carpooling.sv.v1.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class RutaService implements
        CrearRutaCase,
        GetRutaByIdCase,
        GetRutasActivasCase,
        GetAllRutasCase,
        ActualizarRutaCase,
        CambiarEstadoRutaCase {

    private final RutaRepositoryPort rutaRepositoryPort;

    public RutaService(RutaRepositoryPort rutaRepositoryPort) {
        this.rutaRepositoryPort = rutaRepositoryPort;
    }

    @Override
    public Ruta crearRuta(Ruta ruta) {
        if (rutaRepositoryPort.existsByNombre(ruta.nombre())) {
            throw new DuplicateResourceException("Ya existe una ruta registrada con el nombre: " + ruta.nombre());
        }
        return rutaRepositoryPort.save(ruta);
    }

    @Override
    @Transactional(readOnly = true)
    public Ruta getRutaById(UUID id) {
        return rutaRepositoryPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ruta no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ruta> getRutasActivas() {
        return rutaRepositoryPort.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ruta> getAllRutas() {
        return rutaRepositoryPort.findAll();
    }

    @Override
    public Ruta actualizarRuta(UUID id, Ruta rutaModificada) {
        Ruta existente = getRutaById(id);

        String nuevoNombre = rutaModificada.nombre() != null ? rutaModificada.nombre().trim() : existente.nombre();
        if (!nuevoNombre.equalsIgnoreCase(existente.nombre()) && rutaRepositoryPort.existsByNombreAndIdNot(nuevoNombre, id)) {
            throw new DuplicateResourceException("Ya existe otra ruta con el nombre: " + nuevoNombre);
        }

        Ruta actualizada = existente.withUpdatedData(nuevoNombre, rutaModificada.descripcion());
        return rutaRepositoryPort.update(actualizada);
    }

    @Override
    public Ruta cambiarEstadoRuta(UUID id, boolean activo) {
        Ruta existente = getRutaById(id);
        Ruta modificada = existente.withActivo(activo);
        return rutaRepositoryPort.update(modificada);
    }
}
