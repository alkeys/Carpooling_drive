package com.carpooling.sv.v1.shared.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.carpooling.sv.v1.shared.exception.UnauthorizedException;

@Component
public class SecurityContextHelper {

    public SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No hay una sesión de usuario activa");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser;
        }

        throw new UnauthorizedException("El usuario autenticado no es válido");
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getUsername();
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean hasRole(String role) {
        String expected = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(expected));
    }
}
