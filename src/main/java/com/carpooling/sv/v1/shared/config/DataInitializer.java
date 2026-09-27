package com.carpooling.sv.v1.shared.config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.usuario.application.port.out.PasswordEncoderPort;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;
import com.carpooling.sv.v1.usuario.domain.Usuario;
import com.carpooling.sv.v1.usuario.domain.value.EstadoUsuarios;
import com.carpooling.sv.v1.usuario.domain.value.RolUsuario;

/**
 * Inicializador de datos de arranque.
 * Asegura la existencia del usuario Administrador por defecto.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public DataInitializer(UsuarioRepositoryPort usuarioRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public void run(String... args) {
        Optional<Usuario> adminOpt = usuarioRepositoryPort.findByCorreo("admin@admin.com");

        if (adminOpt.isEmpty()) {
            Optional<Usuario> legacyAdminOpt = usuarioRepositoryPort.findByCorreo("admin");
            if (legacyAdminOpt.isPresent()) {
                Usuario legacyAdmin = legacyAdminOpt.get();
                Set<RolUsuario> roles = new HashSet<>(legacyAdmin.roles());
                roles.add(RolUsuario.ADMIN);
                roles.add(RolUsuario.CONDUCTOR);
                roles.add(RolUsuario.CLIENTE);
                roles.add(RolUsuario.USUARIO);

                Usuario updatedLegacy = new Usuario(
                    legacyAdmin.id(),
                    legacyAdmin.nombre(),
                    legacyAdmin.apellido(),
                    "admin@admin.com",
                    legacyAdmin.telefono() != null ? legacyAdmin.telefono() : "+50370000000",
                    passwordEncoderPort.encode("admin"),
                    legacyAdmin.fotourl(),
                    true,
                    EstadoUsuarios.ACTIVO,
                    roles,
                    legacyAdmin.creadoEn(),
                    java.time.OffsetDateTime.now()
                );
                Usuario savedAdmin = usuarioRepositoryPort.update(updatedLegacy);
                log.info("==========================================================================");
                log.info(" [DATA INITIALIZER] Usuario admin migrado exitosamente a admin@admin.com:");
                log.info("  -> ID:         {}", savedAdmin.id());
                log.info("  -> Correo:     admin@admin.com");
                log.info("  -> Contraseña: admin");
                log.info("  -> Roles:      {}", savedAdmin.roles());
                log.info("==========================================================================");
            } else {
                Usuario defaultAdmin = new Usuario(
                    "Admin",
                    "Sistema",
                    "admin@admin.com",
                    "+50370000000",
                    passwordEncoderPort.encode("admin"),
                    null,
                    true,
                    EstadoUsuarios.ACTIVO,
                    Set.of(RolUsuario.ADMIN, RolUsuario.CONDUCTOR, RolUsuario.CLIENTE, RolUsuario.USUARIO)
                );

                Usuario savedAdmin = usuarioRepositoryPort.save(defaultAdmin);
                log.info("==========================================================================");
                log.info(" [DATA INITIALIZER] Usuario Administrador creado exitosamente:");
                log.info("  -> ID:         {}", savedAdmin.id());
                log.info("  -> Correo:     admin@admin.com");
                log.info("  -> Contraseña: admin");
                log.info("  -> Roles:      {}", savedAdmin.roles());
                log.info("==========================================================================");
            }
        } else {
            Usuario existingAdmin = adminOpt.get();
            boolean needsUpdate = false;
            Set<RolUsuario> updatedRoles = new HashSet<>(existingAdmin.roles());
            if (!existingAdmin.hasRole(RolUsuario.ADMIN)) {
                updatedRoles.add(RolUsuario.ADMIN);
                needsUpdate = true;
            }
            if (!passwordEncoderPort.matches("admin", existingAdmin.contrasena())) {
                existingAdmin = existingAdmin.withContrasena(passwordEncoderPort.encode("admin"));
                needsUpdate = true;
            }
            if (needsUpdate) {
                usuarioRepositoryPort.update(existingAdmin.withRoles(updatedRoles));
                log.info(" [DATA INITIALIZER] Usuario admin@admin.com actualizado con rol ADMIN y contraseña configurada.");
            }

            // Limpiar cualquier registro obsoleto con correo "admin"
            usuarioRepositoryPort.findByCorreo("admin").ifPresent(legacy -> {
                usuarioRepositoryPort.delete(legacy);
                log.info(" [DATA INITIALIZER] Usuario obsoleto con correo 'admin' eliminado.");
            });
        }
    }
}
