package com.carpooling.sv.v1.shared.security;

import java.util.Arrays;
import java.util.List;

/**
 * Clase centralizada que define los endpoints del sistema y sus políticas de acceso:
 * - Endpoints públicos (sin autenticación)
 * - Endpoints bloqueados / denegados (lista negra con denyAll())
 * - Endpoints restringidos por roles (ADMIN, CONDUCTOR, CLIENTE, etc.)
 */
public final class SecurityEndpoints {

    private SecurityEndpoints() {
        // Clase de constantes de utilidad, no instanciable
    }

    // =========================================================================
    // 1. ENDPOINTS BLOQUEADOS (DENEGADOS COMPLETAMENTE - denyAll())
    // Agrega aquí cualquier endpoint interno, de depuración o en mantenimiento
    // para bloquearlo inmediatamente en todo el sistema.
    // =========================================================================
    public static final String[] BLOCKED_URLS = {
        "/api/v1/internal/**",
        "/api/v1/debug/**",
        "/api/v1/test/**",
        "/actuator/shutdown"
    };

    // =========================================================================
    // 2. ENDPOINTS PÚBLICOS (GET - permitAll())
    // =========================================================================
    public static final String[] PUBLIC_GET_URLS = {
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/health",
        "/actuator/info",
        "/api/v1/rutas",
        "/api/v1/rutas/*",
        "/api/v1/viajes/buscar",
        "/api/v1/viajes/*",
        "/api/v1/calificaciones/usuario/**",
        "/ws/**"
    };

    // =========================================================================
    // 3. ENDPOINTS PÚBLICOS (POST - permitAll())
    // =========================================================================
    public static final String[] PUBLIC_POST_URLS = {
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
        "/api/v1/auth/register",
        "/api/v1/usuarios" // Registro público de nuevos usuarios
    };

    // =========================================================================
    // 4. ENDPOINTS EXCLUSIVOS PARA ADMINISTRADOR (ROLE_ADMIN)
    // =========================================================================
    public static final String[] ADMIN_URLS = {
        "/api/v1/admin/**",
        "/api/v1/usuarios/*/roles",      // Modificación de roles
        "/api/v1/usuarios/*/estado",     // Suspensión o activación de cuentas
        "/api/v1/rutas/todas"            // Listado administrativo de todas las rutas
    };

    // =========================================================================
    // 5. ENDPOINTS PARA CONDUCTORES (ROLE_CONDUCTOR o ROLE_ADMIN)
    // =========================================================================
    public static final String[] CONDUCTOR_URLS = {
        "/api/v1/conductor/**",
        "/api/v1/vehiculos/**",
        "/api/v1/viajes/mis-viajes",
        "/api/v1/viajes/publicar",
        "/api/v1/viajes/mis-publicaciones/**"
    };

    public static final String[] CLIENTE_URLS = {
        "/api/v1/cliente/**"
    };


    // =========================================================================
    // Métodos utilitarios de consulta
    // =========================================================================
    public static List<String> getBlockedUrls() {
        return Arrays.asList(BLOCKED_URLS);
    }

    public static List<String> getAdminUrls() {
        return Arrays.asList(ADMIN_URLS);
    }

    public static List<String> getConductorUrls() {
        return Arrays.asList(CONDUCTOR_URLS);
    }

    public static List<String> getClienteUrls() {
        return Arrays.asList(CLIENTE_URLS);
    }
}
